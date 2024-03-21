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
import com.michelin.cert.redscan.utils.models.reports.CommonTags;
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
public class UrlcrazyScanApplication {

  private final RabbitTemplate rabbitTemplate;

  /**
   * Constructor to init rabbit template. Only required if pushing data to queues
   *
   * @param rabbitTemplate Rabbit template.
   */
  public UrlcrazyScanApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(UrlcrazyScanApplication.class, args);
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
      LogManager.getLogger(UrlcrazyScanApplication.class).info(String.format("Starting urlcrazy on : %s", masterDomain.getName()));

      JSONArray results = new JSONArray();
      executeUrlCrazy(masterDomain.getName(), "azerty", results);
      executeUrlCrazy(masterDomain.getName(), "qwerty", results);

      if (!results.isEmpty()) {
        try {
          masterDomain.upsertField("urlcrazy", results);
        } catch (DatalakeStorageException e) {
          LogManager.getLogger(UrlcrazyScanApplication.class).info(String.format("datalake storage exception : %s", e));
        }
      }
    } catch (Exception e) {
      LogManager.getLogger(UrlcrazyScanApplication.class).info(String.format("General exception : %s", e));
    }
  }

  private JSONArray executeUrlCrazy(String domain, String keyboardLayout, JSONArray results) {
    try {
      LogManager.getLogger(UrlcrazyScanApplication.class).info(String.format("Starting urlcrazy on : %s with %s layout", domain, keyboardLayout));
      File out = File.createTempFile(keyboardLayout, "out");

      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("/urlcrazy-0.7.3/urlcrazy -k %s -f JSON -o %s %s", keyboardLayout, out.getAbsolutePath(), domain));
      if (streamGobbler != null) {
        LogManager.getLogger(UrlcrazyScanApplication.class).info(String.format("Urlcrazy terminated with status : %d", streamGobbler.getExitStatus()));
        results = jsonResultFormatter(results, domain, out);
      }
    } catch (IOException ex) {
      LogManager.getLogger(UrlcrazyScanApplication.class).error(String.format("IOException : %s", ex.getMessage()));
    }
    return results;
  }

  private JSONArray jsonResultFormatter(JSONArray results, String domain, File file) {
    JSONParser parser = new JSONParser();

    try {
      LogManager.getLogger(UrlcrazyScanApplication.class).info(String.format("Parsing JSON result for %s in %s file.", domain, file.getAbsoluteFile()));
      JSONObject jsonObj = (JSONObject) parser.parse(new FileReader(file));
      JSONArray typos = (JSONArray) jsonObj.get("typos");
      for (Object typo : typos) {
        JSONObject typoJson = (JSONObject) typo;
        if (!typoJson.get("resolved_a").toString().isEmpty()
                && !typoJson.get("type").toString().contains("All SLD")
                && !typoJson.get("type").toString().contains("Wrong TLD")
                && !typoJson.get("type").toString().contains("Original")) {
          String squat = typoJson.get("name").toString();
          if (!results.contains(squat)) {
            results.add(squat);
            LogManager.getLogger(UrlcrazyScanApplication.class).info(String.format("Typo found : %s for %s", squat, domain));
            Vulnerability vulnerability = new Vulnerability(
                    Vulnerability.generateId("redscan-urlcrazy", "POTENTIAL_SQUAT", squat, domain),
                    Severity.INFO,
                    String.format("Potential Squat on %s", squat),
                    String.format("The domain %s may squat the domain : %s", squat, domain),
                    squat,
                    "redscan-urlcrazy",
                    new String[]{CommonTags.PHISHING}
            );

            rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());
          }
        }
      }
    } catch (FileNotFoundException e) {
      LogManager.getLogger(UrlcrazyScanApplication.class).error(String.format("Error with json file: not found : %s", e.toString()));
    } catch (IOException e) {
      LogManager.getLogger(UrlcrazyScanApplication.class).error(String.format("Error with json file: IO : %S", e.toString()));
    } catch (ParseException e) {
      LogManager.getLogger(UrlcrazyScanApplication.class).error(String.format("Error with json file: Parsing %s", e.toString()));
    } finally {
      if (file != null && file.exists()) {
        file.delete();
      }
    }
    return results;
  }
}
