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
import com.michelin.cert.redscan.utils.models.Brand;
import com.michelin.cert.redscan.utils.models.reports.CommonTags;
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import java.io.File;

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

  private static final File EXEC_DIR = new File("/usr/bin/gitgrabber");

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
  @RabbitListener(queues = {RabbitMqConfig.BRAND_DOMAINS})
  public void receiveMessage(String message) {
    try {
      Brand receivedBrand = new Brand();
      receivedBrand.fromJson(message);

      LogManager.getLogger(ScanApplication.class).info(String.format("Start gitgrabber : %s", receivedBrand.getName()));

      //Execute gitgrabber.
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("python3.9 gitGraber.py -k wordlists/keywords.txt -q %s ", receivedBrand.getName()), EXEC_DIR);

      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Gitgrabber terminated with status : %d", streamGobbler.getExitStatus()));

        //Convert the stream output.
        if (streamGobbler.getStandardOutputs() != null) {
          if (streamGobbler.getStandardOutputs().length != 0) {
            int iter = 0;
            int size = streamGobbler.getStandardOutputs().length;
            StringBuilder sbrFull = new StringBuilder(); //For logging purpose.
            StringBuilder sbr = new StringBuilder();     //For datalake.

            while (iter < size) {

              String result = ((String) streamGobbler.getStandardOutputs()[iter]).replaceAll("\u001B\\[[;\\d]*m", "");
              sbrFull.append(result).append(System.getProperty("line.separator"));

              //The contains test is to avoid false positive : https://github.com/certmichelin/Redscan-Gitgrabber/issues/2
              if (result.startsWith("[!]") && result.contains("(keyword used:")) {
                LogManager.getLogger(ScanApplication.class).info(String.format("Detect new vulnerability : %s", result));

                //Begin new vulnerability creation.
                sbr.append(result).append(System.getProperty("line.separator"));
                String title = result.replace("[!] ", "");
                LogManager.getLogger(ScanApplication.class).info(String.format("Extract title : %s", title));

                String url = "";
                String token = "";
                StringBuilder vulnMessage = new StringBuilder();
                boolean vulnEnded = false;

                while (++iter < size && vulnEnded == false) {
                  result = ((String) streamGobbler.getStandardOutputs()[iter]).replaceAll("\u001B\\[[;\\d]*m", "");
                  if (result.startsWith("[+]")) {

                    LogManager.getLogger(ScanApplication.class).info(String.format("Add details to vulnerability : %s", result));
                    sbr.append(result).append(System.getProperty("line.separator"));
                    sbrFull.append(result).append(System.getProperty("line.separator"));

                    //Add line to the message
                    vulnMessage.append(result.replace("[+] ", "")).append(System.getProperty("line.separator"));

                    //Retrieve the raw url.
                    if (result.startsWith("[+] RAW URL : ")) {
                      url = result.replace("[+] RAW URL : ", "");
                      LogManager.getLogger(ScanApplication.class).info(String.format("URL found for vulnerability : %s", url));
                    }

                    if (result.startsWith("[+] Token : ")) {
                      token = result.replace("[+] Token : ", "");
                      LogManager.getLogger(ScanApplication.class).info(String.format("Token found for vulnerability : %s", token));
                    }
                  } else if (result.startsWith("[!]")) {
                    vulnEnded = true;
                  }
                }

                Vulnerability vulnerability = new Vulnerability(
                        Vulnerability.generateId("redscan-gitgrabber", url, token),
                        Severity.HIGH,
                        title,
                        vulnMessage.toString(),
                        url,
                        "redscan-gitgrabber",
                        new String[]{CommonTags.EXPOSURE, CommonTags.CREDENTIALS}
                );

                rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());
              } else {
                ++iter;
              }
            }

            LogManager.getLogger(ScanApplication.class).info(String.format("Gitgrabber output for %s : %s", receivedBrand.getName(), sbrFull.toString()));

            //Update the datalake.
            receivedBrand.upsertField("gitgrabber", sbr.toString());
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
