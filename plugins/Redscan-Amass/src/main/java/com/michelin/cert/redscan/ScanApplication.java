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
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 * @author Sylvain VAISSIER
 * @author Maxence SCHMITT
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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_MASTER_DOMAINS})
  public void receiveMessage(String message) {

    MasterDomain masterDomain = new MasterDomain();
    try {
      masterDomain.fromJson(message);

      //Execute amass.
      LogManager.getLogger(ScanApplication.class).info(String.format("Start amass : %s", masterDomain.getName()));
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();

      // limit dns query (DELETED)
      // limitation on enumeration for time consumption:
      // - only bruteforce with wordlist if 2 sub-subdomains have been found(for better result the min-for-recursive can be remove) (DELETED)
      // - use 50k deepmagic list(for more result better list can be used)
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("amass enum -d %s -brute -w /wordlists/prefixes.txt -nolocaldb", masterDomain.getName()));

      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Amass terminated with status : %d", streamGobbler.getExitStatus()));
        //Convert the stream output to Host List.
        for (Object object : streamGobbler.getStandardOutputs()) {
          try {
            LogManager.getLogger(ScanApplication.class).info(String.format("Amass found : %s", object.toString()));
            Domain domain = new Domain(object.toString(), masterDomain.getId());
            domain.create();
            rabbitTemplate.convertAndSend(domain.getFanoutExchangeName(), "", domain.toJson());
          } catch (DatalakeStorageException ex) {
            LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Storage Exception : %s", ex.getMessage()));
          }
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }
}
