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

import com.michelin.cert.redscan.utils.PermissiveHostVerifier;
import com.michelin.cert.redscan.utils.PermissiveTrustManager;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.models.reports.CommonTags;
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import com.michelin.cert.redscan.utils.models.services.HttpService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;

import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 * @author Sylvain VAISSIER
 */
@SpringBootApplication
public class CertinfoScanApplication {

  private final RabbitTemplate rabbitTemplate;

  private SSLContext sslCtx;

  /**
   * Constructor to init rabbit template. Only required if pushing data to queues
   *
   * @param rabbitTemplate Rabbit template.
   */
  public CertinfoScanApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * Init the SSL configuration.
   */
  @PostConstruct
  public void initSsl() {
    try {
      sslCtx = SSLContext.getInstance("TLS");
      sslCtx.init(null, new TrustManager[]{new PermissiveTrustManager()}, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());

      HttpsURLConnection.setDefaultHostnameVerifier(new PermissiveHostVerifier());
    } catch (KeyManagementException | NoSuchAlgorithmException ex) {
      LogManager.getLogger(CertinfoScanApplication.class).error(String.format("Init SSL error : %s", ex.getMessage()));
    }
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(CertinfoScanApplication.class, args);
  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_HTTP_SERVICES})
  public void receiveMessage(String message) {
    HttpService httpMessage = new HttpService();
    try {
      httpMessage.fromJson(message);
      if (httpMessage.isSsl()) {
        LogManager.getLogger(CertinfoScanApplication.class).info(String.format("Retrieving certificate information on: %s", httpMessage.toUrl()));
        X509Certificate certificate = extractCertificate(httpMessage);
        if (certificate != null) {
          JSONObject extract = extractInformation(certificate, httpMessage);
          if (extract != null) {
            LogManager.getLogger(CertinfoScanApplication.class).info(String.format("Certificate information on %s : %s", httpMessage.toUrl(), extract.toString()));
            try {
              httpMessage.upsertField("certinfo", extract);
            } catch (DatalakeStorageException ex) {
              LogManager.getLogger(CertinfoScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex));
            }
          }
        } else {
          LogManager.getLogger(CertinfoScanApplication.class).warn(String.format("SSLCertificate was not retrieved for %s ", httpMessage.toUrl()));
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(CertinfoScanApplication.class).warn(String.format("General Exception : %s", ex.getMessage()));
    }
  }

  private X509Certificate extractCertificate(HttpService service) {
    X509Certificate certificate = null;
    try {
      URL url = new URL(service.toUrl());
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.connect();
      Certificate cert = conn.getServerCertificates()[0];
      certificate = (cert instanceof X509Certificate) ? (X509Certificate) cert : null;
    } catch (MalformedURLException ex) {
      LogManager.getLogger(CertinfoScanApplication.class).error(String.format("Url %s  was malformed", service.toUrl()));
    } catch (SSLPeerUnverifiedException ex) {
      LogManager.getLogger(CertinfoScanApplication.class).error(String.format("SSL Peer unverified : %s", service.toUrl()));
    } catch (IOException ex) {
      LogManager.getLogger(CertinfoScanApplication.class).error(String.format("IOException for %s : %s", service.toUrl(), ex.getMessage()));
    } catch (Exception ex) {
      LogManager.getLogger(CertinfoScanApplication.class).error(String.format("Exception for %s : %s", service.toUrl(), ex.getMessage()));
    }
    return certificate;
  }

  private JSONObject extractInformation(X509Certificate cert, HttpService service) {
    JSONObject obj = new JSONObject();
    obj.put("isSelfSigned", isSelfSigned(cert, service));
    obj.put("isExpired", isExpired(cert, service));
    obj.put("Deliver to", cert.getSubjectDN().getName());
    obj.put("Issuer", cert.getIssuerDN().getName());
    obj.put("Cipher", cert.getSigAlgName());
    obj.put("notAfter", cert.getNotAfter());
    return obj;
  }

  private boolean isSelfSigned(X509Certificate certificate, HttpService service) {
    boolean isSelfSigned = false;
    if (certificate.getSubjectDN() != null && certificate.getIssuerDN() != null && certificate.getSubjectDN().getName().equals(certificate.getIssuerDN().getName())) {
      LogManager.getLogger(CertinfoScanApplication.class).info(String.format("Certificate on %s is self-signed", service.toUrl()));
      raiseVulnerability(Severity.LOW, service, "self_signed_certificate",
              String.format("Certificated used on %s is self-signed", service.toUrl()),
              String.format("The certificate issuer : %s.", certificate.getIssuerDN().getName()));
      isSelfSigned = true;
    }
    return isSelfSigned;
  }

  private boolean isExpired(X509Certificate certificate, HttpService service) {
    boolean isExpired = false;
    if (certificate.getNotAfter() != null && certificate.getNotAfter().before(new Date())) {
      LogManager.getLogger(CertinfoScanApplication.class).info(String.format("Certificate on %s is expired", service.toUrl()));
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale("EN", "en"));
      raiseVulnerability(Severity.LOW, service, "expired_certificate",
              String.format("Expired certificated used on %s", service.toUrl()),
              String.format("The certificate expired on %s.", df.format(certificate.getNotAfter())));

      isExpired = true;
    }
    return isExpired;
  }

  private void raiseVulnerability(int severity, HttpService service, String vulnName, String title, String message) {
    Vulnerability vuln = new Vulnerability(
            Vulnerability.generateId("redscan-certinfo", vulnName, service.getDomain(),service.getPort()),
            severity,
            title,
            message,
            service.toUrl(),
            "redscan-certinfo",
            new String[]{CommonTags.COMPLIANCE, CommonTags.MISCONFIGURATION}
    );
    rabbitTemplate.convertAndSend(vuln.getFanoutExchangeName(), "", vuln.toJson());
  }

}
