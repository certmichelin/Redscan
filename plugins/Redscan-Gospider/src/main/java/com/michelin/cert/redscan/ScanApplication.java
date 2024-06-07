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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_HTTP_SERVICES})
  public static void receiveMessage(String message) {
    HttpService httpMessage = new HttpService();
    try {
      httpMessage.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Gospider url : %s", httpMessage.toUrl()));
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      String command = String.format("/root/go/bin/gospider -s %s -c 10 -q", httpMessage.toUrl());
      StreamGobbler streamGobbler = osCommandExecutor.execute(command, true);
      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("GoSpider exited with status %s ", streamGobbler.getExitStatus()));
        if (streamGobbler.getExitStatus() == 0) {
          JSONArray results = new JSONArray();
          for (Object object : streamGobbler.getStandardOutputs()) {
            results.put(object);
          }
          LogManager.getLogger(ScanApplication.class).info(String.format("GoSpider output for %s : %s ", httpMessage.toUrl(), results.toString()));
          httpMessage.upsertField("gospider", results);
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Storage Exception : %s", ex.getMessage()));
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }

}
