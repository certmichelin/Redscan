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

import com.michelin.cert.redscan.utils.models.Ip;
import com.michelin.cert.redscan.utils.models.IpRange;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.List;
import java.util.Scanner;

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
 * @author Maxence SCHMITT
 */
@SpringBootApplication
public class ScanApplication {

  private final RabbitTemplate rabbitTemplate;
  private String topTcpPorts;

  /**
   * Constructor to init rabbit template. Only required if pushing data to queues
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
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_IPRANGES})
  public void receiveMessage(String message) {
    IpRange ipRange = new IpRange();
    try {
      ipRange.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Scan Ip Range : %s", ipRange.getCidr()));

      List<String> ips = ipRange.toIpList();
      for (String ip : ips) {
        if (ping(ip) || masscan(ip)) {
          LogManager.getLogger(ScanApplication.class).info(String.format("Ip (%s) reachable", ip));
          Ip ipObj = new Ip(ip, ipRange.getCidr());
          ipObj.create();
          rabbitTemplate.convertAndSend(ipObj.getFanoutExchangeName(), "", ipObj.toJson());
        } else {
          LogManager.getLogger(ScanApplication.class).info(String.format("Ip (%s) not reachable", ip));
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }

  private boolean ping(String ip) {
    LogManager.getLogger(ScanApplication.class).info(String.format("Sending ping request to : %s", ip));
    boolean result = false;
    try {
      InetAddress inet = InetAddress.getByName(ip);
      result = inet.isReachable(3000);
    } catch (UnknownHostException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Unkonwn Host Exception : %s", ex.getMessage()));
    } catch (IOException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Ping IOException : %s", ex.getMessage()));
    }
    return result;
  }

  private boolean masscan(String ip) {
    boolean result = false;
    File masscanOutputFile = null;
    LogManager.getLogger(ScanApplication.class).info(String.format("Start Masscan : %s", ip));
    try {
      masscanOutputFile = File.createTempFile(String.format("masscan_%s_", ip), ".out");

      // The default transmit rate is 100 packets/second.
      // A scan transmits only two packets; 500 ports are scanned.
      // => use a rate of 1000 packets/second
      // (the scan will take longer than 1 second, since Masscan waits 10 seconds for potential TCP SYN+ACK packets)
      // -oL       : list output
      String masscanCmd = String.format("masscan --rate 1000 -oL %s %s -p%s", masscanOutputFile.getAbsolutePath(), ip, topTcpPorts);

      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      StreamGobbler streamGobbler = osCommandExecutor.execute(masscanCmd);

      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Masscan terminated with status : %d", streamGobbler.getExitStatus()));
        if (masscanOutputFile.length() != 0) {
          try ( BufferedReader br = new BufferedReader(new FileReader(masscanOutputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
              if (line.startsWith("open")) {
                result = true;
              }
            }
          }
        }
      }
    } catch (UnknownHostException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("IP not found : %s", ip));
    } catch (IOException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("IOException : %s", ex.getMessage()));
    } finally {
      if (masscanOutputFile != null) {
        masscanOutputFile.delete();
      }
    }
    return result;
  }
}
