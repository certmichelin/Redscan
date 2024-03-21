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
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.models.Domain;
import com.michelin.cert.redscan.utils.models.Ip;
import com.michelin.cert.redscan.utils.models.services.Service;
import com.michelin.cert.redscan.utils.network.NetworkUtils;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;

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

  private final RabbitTemplate rabbitTemplate;
  private String topTcpPorts;

  @Autowired
  private CacheConfig cacheConfig;

  /**
   * Constructor to init rabbit template. Only required if pushing data to
   * queues.
   *
   * @param rabbitTemplate Rabbit template.
   */
  public ScanApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;

    // Init the String from the worldlist file.
    try {
      File topTcpPortsFile = new File("/wordlists/nmap-ports-top1000.txt");
      Scanner reader = new Scanner(topTcpPortsFile);
      topTcpPorts = reader.nextLine();
    } catch (FileNotFoundException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Failed to init the tcp ports wordlist : %s", ex.getMessage()));
    }
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
   * Domain message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_DOMAINS})
  public void receiveDomainMessage(String message) {
    Domain domain = new Domain();
    try {
      domain.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Start Masscan for domain : %s", domain.getName()));
      if (!NetworkUtils.isLocal(domain.getName()) && !NetworkUtils.isLocalhost(domain.getName())) {
        try {
          InetAddress addr = java.net.InetAddress.getByName(domain.getName());
          executeMasscan(addr.getHostAddress(), domain);
        } catch (UnknownHostException ex) {
          LogManager.getLogger(ScanApplication.class).info(String.format("IP not found : %s", domain.getName()));
        }
      } else {
        LogManager.getLogger(ScanApplication.class).warn(String.format("The domain is localhost or internal address : %s", domain.getName()));
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).warn(String.format("General exception : %s", ex.getMessage()));
    }
  }

  /**
   * IP Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_IPS})
  public void receiveIpMessage(String message) {
    Ip ip = new Ip();
    try {
      ip.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Start Masscan for IP : %s", ip.getValue()));
      if (!NetworkUtils.isLocal(ip.getValue()) && !NetworkUtils.isLocalhost(ip.getValue())) {
        executeMasscan(ip.getValue(), ip);
      } else {
        LogManager.getLogger(ScanApplication.class).warn(String.format("The domain is localhost or internal address : %s", ip.getValue()));
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).warn(String.format("General exception : %s", ex.getMessage()));
    }
  }

  private void executeMasscan(String ip, DatalakeStorageItem datalakeStorageItem) {
    File masscanOutputFile = null;
    try {
      //Check if the information is in cache. The cache validity is 24 hours for shodan.
      Map<String, String> cache = cacheConfig.getCache(cacheConfig.buildKey(ip), 24);
      if (cache == null) {
        ArrayList<String> openPorts = new ArrayList<>();
        masscanOutputFile = File.createTempFile(String.format("masscan_%s_", ip), ".out");

        // The default transmit rate is 100 packets/second.
        // A scan transmits only two packets; 500 ports are scanned.
        // => use a rate of 1000 packets/second
        // (the scan will take longer than 1 second, since Masscan waits 10 seconds for potential TCP SYN+ACK packets)
        // -oX       : XML output
        String masscanCmd = String.format("masscan --rate 1000 -oL %s %s -p%s", masscanOutputFile.getAbsolutePath(), ip, topTcpPorts);
        OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
        StreamGobbler streamGobbler = osCommandExecutor.execute(masscanCmd);

        if (streamGobbler != null) {
          LogManager.getLogger(ScanApplication.class).info(String.format("Masscan terminated with status : %d", streamGobbler.getExitStatus()));
          if (masscanOutputFile.length() != 0) {
            try {
              //Parse the output file.
              try ( BufferedReader br = new BufferedReader(new FileReader(masscanOutputFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                  if (line.startsWith("open")) {
                    String port = line.split(" ")[2];
                    LogManager.getLogger(ScanApplication.class).info(String.format("Port found %s:%s", ip, port));
                    openPorts.add(port);
                    Service service = new Service(datalakeStorageItem.getId(), ip, port);
                    rabbitTemplate.convertAndSend(service.getFanoutExchangeName(), "", service.toJson());
                  }
                }
              }

              //Cache the response
              cache = new HashMap<>();
              cache.put("ports", String.join(",", openPorts));
              cacheConfig.setCache(cacheConfig.buildKey(ip), cache);
              
            } catch (IOException ex) {
              LogManager.getLogger(ScanApplication.class).error(ex);
            }
          } else {
            LogManager.getLogger(ScanApplication.class).warn(String.format("OutputFile was empty for : %s", ip));
          }
        } else {
          LogManager.getLogger(ScanApplication.class).warn(String.format("StreamGobbler was null for : %s", ip));
        }
        datalakeStorageItem.upsertField("masscan", openPorts);
        
      } else {
        String cachedOpenPorts = cache.get("ports");
        ArrayList<String> openPorts = new ArrayList<>();
        for (String cachedOpenPort : cachedOpenPorts.split(",")) {
          LogManager.getLogger(ScanApplication.class).info(String.format("Port found %s:%s", ip, cachedOpenPort));
          Service service = new Service(datalakeStorageItem.getId(), ip, cachedOpenPort);
          rabbitTemplate.convertAndSend(service.getFanoutExchangeName(), "", service.toJson());
          openPorts.add(cachedOpenPort);
        }
        datalakeStorageItem.upsertField("masscan", openPorts);
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex.getMessage()));
    } catch (IOException ex) {
      LogManager.getLogger(ScanApplication.class).error(ex);
    } finally {
      if (masscanOutputFile != null) {
        masscanOutputFile.delete();
      }
    }
  }
}
