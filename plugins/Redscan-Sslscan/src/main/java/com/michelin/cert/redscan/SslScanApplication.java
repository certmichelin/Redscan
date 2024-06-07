/*
 * Copyright 2021 Michelin CERT (https://cert.michelin.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michelin.cert.redscan;

import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.models.reports.CommonTags;
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import com.michelin.cert.redscan.utils.models.services.HttpService;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 * @author Sylvain VAISSIER
 * @author Maxence SCHMITT
 */
@SpringBootApplication
public class SslScanApplication {

  //Only required if pushing data to queues
  private final RabbitTemplate rabbitTemplate;

  /**
   * Constructor to init rabbit template. Only required if pushing data to queues.
   *
   * @param rabbitTemplate Rabbit template.
   */
  public SslScanApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(SslScanApplication.class, args);
  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_HTTP_SERVICES})
  public void receiveMessage(String message) {
    File sslscanOutputFile = null;
    HttpService service = new HttpService();
    try {
      service.fromJson(message);
      LogManager.getLogger(SslScanApplication.class).info(String.format("Start sslscan : %s", service.toUrl()));

      if (service.isSsl()) {
        try {
          sslscanOutputFile = File.createTempFile(String.format("sslscan_%s_%s", service.getDomain(), service.getPort()), ".xml");
          OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
          StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("sslscan --no-colour --timeout=10 --xml=%s %s", sslscanOutputFile.getAbsolutePath(), service.toUrl()));

          if (streamGobbler != null) {
            LogManager.getLogger(SslScanApplication.class).info(String.format("Ssslscan terminated with status : %d", streamGobbler.getExitStatus()));
            if (streamGobbler.getErrorOutputs() != null && streamGobbler.getErrorOutputs().length > 0) {
              StringBuilder errorMessage = new StringBuilder();
              for (Object errorOuput : streamGobbler.getErrorOutputs()) {
                errorMessage.append(errorOuput);
              }
              LogManager.getLogger(SslScanApplication.class).warn(String.format("SSLScan error message : %s", errorMessage.toString()));
            }

            JSONObject data = parseXml(sslscanOutputFile.getAbsolutePath(), service);
            if (data != null) {
              LogManager.getLogger(SslScanApplication.class).info(String.format("SSLScan extraction : %s", data.toString()));
              service.upsertField("SSLScan", data);
            } else {
              LogManager.getLogger(SslScanApplication.class).warn(String.format("Empty data for %s", service.toUrl()));
            }
          }
        } catch (DatalakeStorageException ex) {
          LogManager.getLogger(SslScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex));
        } catch (IOException ex) {
          LogManager.getLogger(SslScanApplication.class).error(String.format("IOException : %s", ex));
        } finally {
          if (sslscanOutputFile != null) {
            sslscanOutputFile.delete();
          }
        }
      } else {
        LogManager.getLogger(SslScanApplication.class).info(String.format(" %s on port %s is not an HTTPS SERVICE", service.getDomain(), service.getPort()));
      }
    } catch (Exception ex) {
      LogManager.getLogger(SslScanApplication.class).error(String.format("General exception : %s", ex));
    }
  }

  private JSONObject parseXml(String filename, HttpService service) {
    SAXBuilder sax = new SAXBuilder();
    JSONObject result = new JSONObject();

    try {
      Document doc = sax.build(new File(filename));
      Element rootNode = doc.getRootElement();
      Element ssltestNode = rootNode.getChild("ssltest");

      //Check protocols.
      JSONArray jsonProtocols = new JSONArray();
      List<Element> protocolNodes = ssltestNode.getChildren("protocol");
      for (Element protocolNode : protocolNodes) {
        if (getSafeAttribute(protocolNode, "enabled").equals("1")) {
          JSONObject jsonProtocol = new JSONObject();
          jsonProtocol.put("type", getSafeAttribute(protocolNode, "type"));
          jsonProtocol.put("version", getSafeAttribute(protocolNode, "version"));
          jsonProtocols.add(jsonProtocol);

          //Raise protocol misconfiguration.
          if (getSafeAttribute(protocolNode, "type").equals("ssl")) {
            raiseVulnerability(Severity.MEDIUM, service, "ssl_protocol",
                    String.format("SSL protocol is enabled for %s", service.toUrl()),
                    "Weak encrytion protocol was found, HTTPS websites should not be used with SSL protocol.");
          }

          if (getSafeAttribute(protocolNode, "type").equals("tls")
                  && (getSafeAttribute(protocolNode, "version").equals("1.0") || getSafeAttribute(protocolNode, "version").equals("1.1"))) {
            raiseVulnerability(Severity.LOW, service, "tls_protocol",
                    String.format("Weak TLS protocol is enabled for %s", service.toUrl()),
                    "Weak TLS protocol was found, HTTPS websites should not be used with TLSv1.0 or TLSv1.1 protocols.");
          }
        }
      }
      result.put("enabledProtocols", jsonProtocols);

      //Appends fallback, renegotation and compression data.
      result.put("fallback", getSafeAttribute(ssltestNode.getChild("fallback"), "supported"));
      result.put("compression", getSafeAttribute(ssltestNode.getChild("compression"), "supported"));
      JSONObject renegociationObject = new JSONObject();
      renegociationObject.put("supported", getSafeAttribute(ssltestNode.getChild("renegotiation"), "supported"));
      renegociationObject.put("secure", getSafeAttribute(ssltestNode.getChild("renegotiation"), "secure"));
      result.put("renegotiation", renegociationObject);

      //Check heartbleed.
      JSONArray jsonHeartbleeds = new JSONArray();
      List<Element> heartbleedNodes = ssltestNode.getChildren("heartbleed");
      for (Element heartbleedNode : heartbleedNodes) {
        if (getSafeAttribute(heartbleedNode, "vulnerable").equals("1")) {
          JSONObject jsonHeartbleed = new JSONObject();
          jsonHeartbleed.put("sslversion", getSafeAttribute(heartbleedNode, "sslversion"));
          jsonHeartbleeds.add(jsonHeartbleed);

          raiseVulnerability(Severity.CRITICAL, service, "ssl_heartbleed",
                  String.format("Heartbleed vulnerability on %s", service.toUrl()),
                  "Heartbleed was a security bug in the OpenSSL cryptography library.");
        }
      }
      result.put("heartbleedVulnerabilities", jsonHeartbleeds);

      //Check ciphers.
      JSONArray jsonCiphers = new JSONArray();
      List<Element> cipherNodes = ssltestNode.getChildren("cipher");
      for (Element cipherNode : cipherNodes) {

        JSONObject jsonCipher = new JSONObject();
        jsonCipher.put("status", getSafeAttribute(cipherNode, "status"));
        jsonCipher.put("sslversion", getSafeAttribute(cipherNode, "sslversion"));
        jsonCipher.put("bits", getSafeAttribute(cipherNode, "bits"));
        jsonCipher.put("cipher", getSafeAttribute(cipherNode, "cipher"));
        jsonCipher.put("strength", getSafeAttribute(cipherNode, "strength"));
        jsonCiphers.add(jsonCipher);

        if (!getSafeAttribute(cipherNode, "strength").equals("acceptable") && !getSafeAttribute(cipherNode, "strength").equals("strong")) {
          raiseVulnerability(Severity.INFO, service, "weak_cipher",
                  String.format("Weak cipher used on %s", service.toUrl()),
                  String.format("Cipher %s is considered not strong.", getSafeAttribute(cipherNode, "cipher")));
        }
      }
      result.put("ciphers", jsonCiphers);

      //Add groups.
      JSONArray jsonGroups = new JSONArray();
      List<Element> groupNodes = ssltestNode.getChildren("group");
      for (Element groupNode : groupNodes) {
        JSONObject jsonGroup = new JSONObject();
        jsonGroup.put("sslversion", getSafeAttribute(groupNode, "sslversion"));
        jsonGroup.put("bits", getSafeAttribute(groupNode, "bits"));
        jsonGroup.put("name", getSafeAttribute(groupNode, "name"));
        jsonGroups.add(jsonGroup);
      }
      result.put("group", jsonGroups);

    } catch (IOException ex) {
      LogManager.getLogger(SslScanApplication.class).error(String.format("IOException : %s", ex));
    } catch (JDOMException ex) {
      Logger.getLogger(SslScanApplication.class.getName()).log(Level.SEVERE, null, ex);
    }
    return result;

  }

  private void raiseVulnerability(int severity, HttpService service, String vulnName, String title, String message) {
    Vulnerability vuln = new Vulnerability(
            Vulnerability.generateId("redscan-sslscan", vulnName, service.getDomain(),service.getPort()),
            severity,
            title,
            message,
            service.toUrl(),
            "redscan-sslscan",
            new String[]{CommonTags.COMPLIANCE}
    );
    
    rabbitTemplate.convertAndSend(vuln.getFanoutExchangeName(), "", vuln.toJson());
  }

  private String getSafeAttribute(Element element, String attributeName) {
    String result = "";
    if (element != null) {
      Attribute attribute = element.getAttribute(attributeName);
      if (attribute != null) {
        result = attribute.getValue();
      }
    }
    return result;
  }

}
