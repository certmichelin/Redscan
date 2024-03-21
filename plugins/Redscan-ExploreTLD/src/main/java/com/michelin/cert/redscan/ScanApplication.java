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

import com.michelin.cert.redscan.utils.models.Brand;
import com.michelin.cert.redscan.utils.models.MasterDomain;
import com.michelin.cert.redscan.utils.models.ServiceLevel;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import java.io.BufferedReader;
import java.io.FileReader;

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

  public final String suffixFile = "/wordlists/public_suffix_list.dat";

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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_BRANDS})
  public void receiveMessage(String message) {
    try {
      Brand brand = new Brand();
      brand.fromJson(message);

      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      LogManager.getLogger(ScanApplication.class).info(String.format("Start exploretld : %s", brand.getName()));

      BufferedReader br = new BufferedReader(new FileReader(suffixFile));
      //For each tlds
      for (String tld; (tld = br.readLine()) != null;) {
        if (tld != null && !tld.isEmpty()) {

          // Stop reading file before private domains
          if (tld.startsWith("// ===END ICANN DOMAINS===")) {
            LogManager.getLogger(ScanApplication.class).debug(String.format("stop before PRIVATE domains: %s", tld));
            break;
          }

          // trim comments line + domain we don't want
          if (!tld.startsWith("//") && !tld.startsWith("gov.") && !tld.startsWith("mil.") && !tld.contains(".gov.") && !tld.contains(".mil.")) {

            LogManager.getLogger(ScanApplication.class).info(String.format("processing exploretld: %s for brand %s", tld, brand.getName()));

            //Execute dig.
            StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("dig +short %s.%s", brand.getName(), tld));

            if (streamGobbler != null) {
              //If there is an output we suppose entry is existing
              if (streamGobbler.isHavingStdOuput()) {
                LogManager.getLogger(ScanApplication.class).info(String.format("Master domain has been found :%s.%s", brand.getName(), tld));
                MasterDomain masterDomain = new MasterDomain(String.format("%s.%s", brand.getName(), tld), ServiceLevel.GOLD.getValue(), false, false, null, brand.getName());
                masterDomain.create();
              } else {
                LogManager.getLogger(ScanApplication.class).info(String.format("Master domain not found :%s.%s", brand.getName(), tld));
              }
            }
          }
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Exception : %s", ex));
    }
  }

}
