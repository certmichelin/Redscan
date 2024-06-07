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
import com.michelin.cert.redscan.utils.models.MasterDomain;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_MASTERDOMAINS})
  public void receiveMessage(String message) {
    MasterDomain masterDomain = new MasterDomain();
    try {
      masterDomain.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Start amass reversewhois: %s", masterDomain.getName()));
      //Execute Amass-reversewhois.
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("amass intel -exclude NetworksDB -whois -d %s,@%s", masterDomain.getName(), masterDomain.getName()));

      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Amass reversewhois terminated with status : %d", streamGobbler.getExitStatus()));
        //Convert the stream output to Host List.
        for (Object object : streamGobbler.getStandardOutputs()) {
          try {
            LogManager.getLogger(ScanApplication.class).info(String.format("Master domains found : %s", object.toString()));
            MasterDomain newMasterDomain = new MasterDomain(object.toString(), masterDomain.getName());
            newMasterDomain.create();
          } catch (DatalakeStorageException ex) {
            LogManager.getLogger(ScanApplication.class).error(String.format("DatalakeStorage exception : %s", ex.getMessage()));
          }
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception : %s", ex.getMessage()));
    }
  }

}
