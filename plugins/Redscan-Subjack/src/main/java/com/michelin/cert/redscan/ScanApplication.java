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
import com.michelin.cert.redscan.utils.models.Domain;
import com.michelin.cert.redscan.utils.models.reports.CommonTags;
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_DOMAINS})
  public void receiveMessage(String message) {
    Domain domain = new Domain();
    try {
      domain.fromJson(message);

      LogManager.getLogger(ScanApplication.class).info(String.format("Start subjack : %s", domain.getName()));

      //Execute Subjack.
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("/root/go/bin/subjack -c /root/go/src/github.com/certmichelin/subjack/fingerprints.json -a -m -d %s ", domain.getName()));

      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Subjack terminated with status : %d", streamGobbler.getExitStatus()));

        //Convert the stream output.
        if (streamGobbler.getStandardOutputs() != null) {
          if (streamGobbler.getStandardOutputs().length != 0) {
            for (Object object : streamGobbler.getStandardOutputs()) {
              String result = ((String) object).replaceAll("\u001B\\[[;\\d]*m", "");
              LogManager.getLogger(ScanApplication.class).info(String.format("Subjack output : %s", result));
              if (result.startsWith("[")) { //Remove potential error.
                String[] tmp = result.split(" ");
                StringBuilder subjackOutput = new StringBuilder();
                for (int i = 0; i < tmp.length - 1; i++) {
                  subjackOutput.append(tmp[i]).append(" ");
                }
                domain.upsertField("subjack", subjackOutput);

                //Send the vulnerability.
                Vulnerability vulnerability = new Vulnerability(
                        Vulnerability.generateId("redscan-subjack", domain.getName()),
                        Severity.HIGH,
                        String.format("[%s] Subdomain potentially takeoverable", domain.getName()),
                        String.format("The domain %s is potentially takeoverable : %s", domain.getName(), subjackOutput),
                        domain.getName(),
                        "redscan-subjack",
                        new String[]{CommonTags.THREAT, CommonTags.VULNERABILITY}
                );

                rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());
              }
            }
          } else {
            domain.upsertField("subjack", "None");
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Storage Exception : %s", ex.getMessage()));
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Exception : %s", ex.getMessage()));
    }
  }

}
