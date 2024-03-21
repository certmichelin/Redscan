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
import com.michelin.cert.redscan.utils.models.services.HttpService;
import com.michelin.cert.redscan.utils.models.services.Service;
import com.michelin.cert.redscan.utils.network.NetworkUtils;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class ScanApplication {

  @Autowired
  private CacheConfig cacheConfig;

  private final RabbitTemplate rabbitTemplate;

  /**
   * Constructor to init rabbit template. Only required if pushing data to queues
   *
   * @param rabbitTemplate Rabbit template.
   */
  public ScanApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(ScanApplication.class, args);
  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_SERVICES})
  public void receiveMessage(String message) {
    Service service = new Service();

    try {
      service.fromJson(message);

      File nmapOutputFile = null;
      LogManager.getLogger(ScanApplication.class).info(String.format("Start nmap service : %s:%s", service.getIp(), service.getPort()));

      if (!NetworkUtils.isLocal(service.getDomain()) && !NetworkUtils.isLocalhost(service.getDomain())) {
        //Check if the information is in cache. The cache validity is 24 hours for nmap-service.
        Map<String, String> cache = cacheConfig.getCache(cacheConfig.buildKey(service.getIp(), service.getPort()), 24);
        if (cache == null) {
          try {
            nmapOutputFile = File.createTempFile(String.format("nmap_%s_%s_", service.getIp(), service.getPort()), ".out");

            // --allports: do not excludes "special" ports such as 9100 (used by printers);
            // -oX             : XML output
            // -sV             : Service Detection
            // --script=banner : NSE Script to obtain the banner.
            // -Pn             : Force the scan, if icmp is disabled.
            String nmapCmd = String.format("nmap %s --allports -oX %s -sV -sS -sU -A --script=banner -Pn -p %s", service.getIp(), nmapOutputFile.getAbsolutePath(), service.getPort());

            OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
            StreamGobbler streamGobbler = osCommandExecutor.execute(nmapCmd);

            if (streamGobbler != null) {
              LogManager.getLogger(ScanApplication.class).info(String.format("Nmap terminated with status : %d", streamGobbler.getExitStatus()));
              cache = new HashMap<>();
              List<Service> discoveredServices = handleNmapXml(nmapOutputFile, service);
              for (Service discoveredService : discoveredServices) {
                try {
                  discoveredService.setDomain(service.getDomain());
                  discoveredService.setPort(service.getPort());
                  discoveredService.setIp(service.getIp());
                  discoveredService.create();
                  serviceRedirector(service.getDomain(), service.getIp(), service.getPort(), discoveredService.getProtocol(), analyzeService(discoveredService));
                  cache.put(discoveredService.getProtocol(), discoveredService.toJson());
                } catch (DatalakeStorageException ex) {
                  LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex.getMessage()));
                }
              }
              cacheConfig.setCache(cacheConfig.buildKey(service.getIp(), service.getPort()), cache);
            }
          } catch (IOException ex) {
            LogManager.getLogger(ScanApplication.class).error(String.format("Issue with the temp file : %s", ex.getMessage()));
          } finally {
            if (nmapOutputFile != null) {
              nmapOutputFile.delete();
            }
          }
        } else {
          //Retrieve data from cache.
          Collection<String> jsonCachedServices = cache.values();
          for (String jsonCachedService : jsonCachedServices) {
            Service cachedService = new Service();
            try {
              cachedService.fromJson(jsonCachedService);
              cachedService.setDomain(service.getDomain());
              cachedService.setPort(service.getPort());
              cachedService.setIp(service.getIp());
              cachedService.create();
              serviceRedirector(service.getDomain(), service.getIp(), service.getPort(), cachedService.getProtocol(), analyzeService(cachedService));
            } catch (DatalakeStorageException ex) {
              LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex.getMessage()));
            }
          }
        }
      } else {
        LogManager.getLogger(ScanApplication.class).warn(String.format("The domain is localhost or internal address : %s", message));
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception : %s", ex.getMessage()));
    }
  }

  private List<Service> handleNmapXml(File nmapOutputFile, Service service) {
    List<Service> discoveredServices = new ArrayList<>();
    try {
      SAXBuilder builder = new SAXBuilder();
      Document document = (Document) builder.build(nmapOutputFile);

      Element rootNode = document.getRootElement();
      Element hostNode = rootNode.getChild("host");
      if (hostNode != null) {
        Element portsNode = hostNode.getChild("ports");
        if (portsNode != null) {
          List<Element> portNodes = portsNode.getChildren("port");

          if (portNodes != null) {
            for (Element portNode : portNodes) {

              //Instanciate the new discovered service.
              Service discoveredService = new Service();

              //Retrieve protocol.
              String protocol = portNode.getAttributeValue("protocol");
              if (protocol != null) {
                LogManager.getLogger(ScanApplication.class).info(String.format("Protocol found for %s:%s %s", service.getDomain(), service.getPort(), protocol));
                discoveredService.setProtocol(protocol);
              } else {
                LogManager.getLogger(ScanApplication.class).info(String.format("Protocol not found for %s:%s", service.getDomain(), service.getPort()));
                discoveredService.setProtocol("not found");
              }

              //Retrieve state.
              Element stateNode = portNode.getChild("state");
              if (stateNode != null) {
                String state = stateNode.getAttributeValue("state");
                if (state != null) {
                  LogManager.getLogger(ScanApplication.class).info(String.format("State found for %s:%s %s", service.getDomain(), service.getPort(), state));
                  discoveredService.setState(state);
                } else {
                  LogManager.getLogger(ScanApplication.class).info(String.format("State not found for %s:%s", service.getDomain(), service.getPort()));
                  discoveredService.setState("not found");
                }
              }

              //Retrieve service information.
              Element serviceNode = portNode.getChild("service");
              if (serviceNode != null) {

                //Retrieve service.
                String serviceStr = serviceNode.getAttributeValue("name");
                if (serviceStr != null) {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Service found for %s:%s %s", service.getDomain(), service.getPort(), serviceStr));
                  discoveredService.setName(serviceStr);
                } else {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Service not found for %s:%s", service.getDomain(), service.getPort()));
                  discoveredService.setName("not found");
                }

                //Retrieve Tunnel.
                String tunnelStr = serviceNode.getAttributeValue("tunnel");
                if (tunnelStr != null) {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Tunnel found for %s:%s %s", service.getDomain(), service.getPort(), serviceStr));
                  discoveredService.setTunnel(tunnelStr);
                } else {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Tunnel not found for %s:%s", service.getDomain(), service.getPort()));
                  discoveredService.setTunnel("not found");
                }

                //Retrieve product.
                String product = serviceNode.getAttributeValue("product");
                if (product != null) {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Product found for %s:%s %s", service.getDomain(), service.getPort(), product));
                  discoveredService.setProduct(product);
                } else {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Product not found for %s:%s", service.getDomain(), service.getPort()));
                  discoveredService.setProduct("not found");
                }

                //Retrieve version.
                String version = serviceNode.getAttributeValue("version");
                if (version != null) {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Version found for %s:%s %s", service.getDomain(), service.getPort(), version));
                  discoveredService.setVersion(version);
                } else {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Version not found for %s:%s", service.getDomain(), service.getPort()));
                  discoveredService.setVersion("not found");
                }
              }

              //Retrieve service information.
              Element scriptNode = portNode.getChild("script");
              if (scriptNode != null) {
                //Retrieve service.
                String banner = scriptNode.getAttributeValue("output");
                if (banner != null) {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Banner found for %s:%s %s", service.getDomain(), service.getPort(), banner));
                  discoveredService.setBanner(banner);
                } else {
                  LogManager.getLogger(ScanApplication.class).info(String.format("Banner not found for %s:%s", service.getDomain(), service.getPort()));
                  discoveredService.setBanner("not found");
                }
              }
              discoveredServices.add(discoveredService);
            }
          } else {
            LogManager.getLogger(ScanApplication.class).info(String.format("Port tags not found for %s:%s", service.getDomain(), service.getPort()));
          }
        } else {
          LogManager.getLogger(ScanApplication.class).info(String.format("Ports tags not found for %s:%s", service.getDomain(), service.getPort()));
        }
      } else {
        LogManager.getLogger(ScanApplication.class).info(String.format("Host tags not found for %s:%s", service.getDomain(), service.getPort()));
      }
    } catch (JDOMException | IOException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Error during the xml parsing : %s", ex.getMessage()));
    }
    return discoveredServices;
  }

  private void serviceRedirector(String domain, String ip, String port, String protocol, String analyzedService) {
    //Point the service to the correct fanout.
    try {
      if (analyzedService != null) {
        switch (analyzedService) {
          case "HTTPS_SERVICE":
            HttpService httpService = new HttpService(domain, ip, port, protocol, true);
            httpService.create();
            rabbitTemplate.convertAndSend(httpService.getFanoutExchangeName(), "", httpService.toJson());
            break;
          case "HTTP_SERVICE":
            httpService = new HttpService(domain, ip, port, protocol, false);
            httpService.create();
            rabbitTemplate.convertAndSend(httpService.getFanoutExchangeName(), "", httpService.toJson());
            break;
          default:
            LogManager.getLogger(ScanApplication.class).info(String.format("Service Injection : %s is not yet supported", analyzedService));
            break;
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex.getMessage()));
    }
  }

  /**
   * Analyse the result from NMAP to point the service to the correct fanout.
   *
   * @param service Service to analyze.
   * @return Return the service pushed.
   */
  private String analyzeService(Service service) {
    String result = null;

    String name = service.getName();
    String tunnel = service.getTunnel();
    String state = service.getState();
    String banner = service.getBanner();

    //Detecting type open service.
    if (name != null && state.equalsIgnoreCase("open")) {

      //HTTPS
      if (name.equalsIgnoreCase("https") || name.equalsIgnoreCase("https-alt") || name.equalsIgnoreCase("tungsten-https") || name.equalsIgnoreCase("ssl")) {
        result = "HTTPS_SERVICE";

      } else if (tunnel != null && (name.equalsIgnoreCase("http") || name.equalsIgnoreCase("http-proxy")) && tunnel.equalsIgnoreCase("ssl")) {
        result = "HTTPS_SERVICE";
      }

      //HTTP
      if (result == null && (name.equalsIgnoreCase("http") || name.equalsIgnoreCase("http-proxy"))) {
        result = "HTTP_SERVICE";
      }

      //FTP
      if (result == null && name.equalsIgnoreCase("ftp")) {
        result = "FTP_SERVICE";
      }

      //DNS
      if (result == null && name.equalsIgnoreCase("domain")) {
        result = "DNS_SERVICE";
      }

      //IMAP/IMAPS
      if (result == null && (name.equalsIgnoreCase("imap") || name.equalsIgnoreCase("imaps"))) {
        result = "IMAP_SERVICE";
      }

      //MySQL
      if (result == null && name.equalsIgnoreCase("mysql")) {
        result = "MYSQL_SERVICE";
      }
      
      //PostgreDB 
      if (result == null && name.equalsIgnoreCase("postgresql")) {
        result = "POSTGRESQL_SERVICE";
      }

      //Oracle DB
      if (result == null && name.equalsIgnoreCase("oracle-tns")) {
        result = "ORACLEDB_SERVICE";
      }

      //POP3/POP3S
      if (result == null && (name.equalsIgnoreCase("pop3") || name.equalsIgnoreCase("pop3s"))) {
        result = "POP_SERVICE";
      }

      //RPCBIND
      if (result == null && name.equalsIgnoreCase("rpcbind")) {
        result = "RPCBIND_SERVICE";
      }

      //SIP
      if (result == null && (name.equalsIgnoreCase("sip") || name.equalsIgnoreCase("sip-tls"))) {
        result = "SIP_SERVICE";
      }

      //SMTP
      if (result == null && (name.equalsIgnoreCase("smtp") || name.equalsIgnoreCase("smtps") || name.equalsIgnoreCase("submission"))) {
        result = "SMTP_SERVICE";
      }

      //SSH
      if (result == null && name.equalsIgnoreCase("ssh")) {
        result = "SSH_SERVICE";
      }

      //RLOGIN
      if (result == null && name.equalsIgnoreCase("login")) {
        result = "RLOGIN_SERVICE";
      }

      //Java Object Serialization
      if (result == null && name.equalsIgnoreCase("java-object")) {
        result = "JAVA_BIN_SERVICE";
      }

      //LDAP
      if (result == null && name.equalsIgnoreCase("ldap")) {
        result = "LDAP_SERVICE";
      }

      //SMB
      if (result == null && name.equalsIgnoreCase("microsoft-ds")) {
        result = "SMB_SERVICE";
      }

      //RDP
      if (result == null && name.equalsIgnoreCase("ms-wbt-server")) {
        result = "RDP_SERVICE";
      }

      //RPC
      if (result == null && name.equalsIgnoreCase("msrpc")) {
        result = "RPC_SERVICE";
      }

      //NFS
      if (result == null && name.equalsIgnoreCase("nfs")) {
        result = "NFS_SERVICE";
      }

      //SQUID
      if (result == null && name.equalsIgnoreCase("squid-http")) {
        result = "SQUID_SERVICE";
      }

      //NETBIOS-SSN
      if (result == null && name.equalsIgnoreCase("netbios-ssn")) {
        result = "NETBIOS-SSN_SERVICE";
      }

      //SNMP
      if (result == null && name.equalsIgnoreCase("snmp")) {
        result = "SNMP_SERVICE";
      }

      //RSFTP
      if (result == null && name.equalsIgnoreCase("rsftp")) {
        result = "RSFTP_SERVICE";
      }

      //PPTP
      if (result == null && name.equalsIgnoreCase("pptp")) {
        result = "PPTP_SERVICE";
      }

      //XMPP
      if (result == null && name.equalsIgnoreCase("xmpp-server")) {
        result = "XMPP_SERVICE";
      }

      //Try to guess exotic services from banner.(Specially HTTP/HTTPS service)
      if (result == null && banner != null && !banner.isEmpty()) {
        if (banner.contains("HTTP/1") || banner.contains("HTTP/2")) {
          if (tunnel != null && tunnel.equalsIgnoreCase("ssl")) {
            result = "HTTPS_SERVICE";
          } else {
            result = "HTTP_SERVICE";
          }
        }

        if (result == null && banner.contains("AMQP:handshake")) {
          result = "AMQP_SERVICE";
        }

        if (result == null && banner.contains("SSH-2.0")) {
          result = "SSH_SERVICE";
        }
      }
    }

    try {
      if (result != null) {
        service.upsertField("service_sent", result);
      } else {
        service.upsertField("service_sent", "NOT_FOUND");
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ScanApplication.class
      ).error(String.format("Datalake Strorage exception : %s", ex.getMessage()));
    }

    return result;
  }
}
