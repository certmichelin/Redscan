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
import com.michelin.cert.redscan.utils.models.MasterDomain;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan Subfinder main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class SubfinderScanApplication {

  private final RabbitTemplate rabbitTemplate;

  /**
   * Constructor to init rabbit template.
   *
   * @param rabbitTemplate Rabbit template.
   */
  public SubfinderScanApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(SubfinderScanApplication.class, args);
  }

  /**
   * Master domain message executor
   *
   * @param message Master domain received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_MASTERDOMAINS})
  public void receiveDomainMessage(String message) {
    try {
      MasterDomain masterDomain = new MasterDomain();
      masterDomain.fromJson(message);

      //Execute Subfinder.
      LogManager.getLogger(SubfinderScanApplication.class).info(String.format("Start subfinder : %s", masterDomain.getName()));
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("subfinder -silent -nW -d %s", masterDomain.getName()));

      if (streamGobbler != null) {
        LogManager.getLogger(SubfinderScanApplication.class).info(String.format("Subfinder terminated with status : %d", streamGobbler.getExitStatus()));
        //Convert the stream output to Host List.
        for (Object object : streamGobbler.getStandardOutputs()) {
          try {
            LogManager.getLogger(SubfinderScanApplication.class).info(String.format("Subdomain found : %s", object.toString()));
            if (!object.toString().contains("passivetotal") && !object.toString().contains("virustotal")) {
              Domain domain = new Domain(object.toString(), masterDomain.getName());
              domain.create();
              rabbitTemplate.convertAndSend(domain.getFanoutExchangeName(), "", domain.toJson());
            }
          } catch (DatalakeStorageException ex) {
            LogManager.getLogger(SubfinderScanApplication.class).error(String.format("DatalakeStorage exception : %s", ex.getMessage()));
          }
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(SubfinderScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }

}
