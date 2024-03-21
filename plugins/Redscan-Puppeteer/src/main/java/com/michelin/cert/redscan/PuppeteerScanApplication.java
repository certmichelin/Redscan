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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.Base64;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Florent BORDIGNON
 * @author Sylvain VAISSIER
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class PuppeteerScanApplication {

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(PuppeteerScanApplication.class, args);
  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_HTTP_SERVICES})
  public void receiveMessage(String message) {

    HttpService httpService = new HttpService();
    try {
      httpService.fromJson(message);
      LogManager.getLogger(PuppeteerScanApplication.class).info(String.format("Start Puppeteer service : %s", httpService.toUrl()));

      try {
        String outFileName = String.format("/tmp/%s_%s.png", httpService.getIp(), httpService.getPort());

        OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
        StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("node /usr/local/bin/screenshot_puppeteer.js %s %s", httpService.toUrl(), outFileName));

        if (streamGobbler != null) {
          if (streamGobbler.getExitStatus() != 0) {
            LogManager.getLogger(PuppeteerScanApplication.class).error(String.format("Puppeteer terminated with status : %d", streamGobbler.getExitStatus()));
          } else {
            LogManager.getLogger(PuppeteerScanApplication.class).info(String.format("Puppeteer terminated with status : %d", streamGobbler.getExitStatus()));

            File outFile = new File(outFileName);
            String outFileBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(outFile.toPath()));
            outFile.delete();
            httpService.upsertField("puppeteer", outFileBase64);
          }
        }
      } catch (DatalakeStorageException ex) {
        LogManager.getLogger(PuppeteerScanApplication.class).error(String.format("Datalake Storage Exception : %s", ex.getMessage()));
      } catch (IOException ex) {
        LogManager.getLogger(PuppeteerScanApplication.class).error(String.format("IOException : %s", ex.getMessage()));
      } catch (Exception ex) {
        LogManager.getLogger(PuppeteerScanApplication.class).error(String.format("Exception : %s", ex.getMessage()));
      }
    } catch (Exception ex) {
      LogManager.getLogger(PuppeteerScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }
}
