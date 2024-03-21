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
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import kong.unirest.json.JSONArray;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_DOMAINS})
  public void receiveMessage(String message) {
    Domain domain = new Domain();
    try {
      domain.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Start cname : %s", domain.getName()));
      //Execute dig.
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("dig CNAME +short %s", domain.getName()));

      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("cname terminated with status : %d", streamGobbler.getExitStatus()));

        //Convert the stream output to cname entry.
        JSONArray resultArray = new JSONArray();
        for (Object object : streamGobbler.getStandardOutputs()) {
          //Strip the last dot and send the CNAME
          String result = object.toString().substring(0, object.toString().length() - 1);
          resultArray.put(result);
        }

        try {
          if (!resultArray.isEmpty()) {

            LogManager.getLogger(ScanApplication.class).info(String.format("cname found for %s: %s", domain.getName(), resultArray.toString()));
            domain.upsertField("cname", resultArray);
          } else {
            LogManager.getLogger(ScanApplication.class).info(String.format("No cname found for %s", domain.getName()));
            domain.upsertField("cname", "None");
          }
        } catch (DatalakeStorageException ex) {
          LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex));
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception : %s", ex));
    }
  }

}
