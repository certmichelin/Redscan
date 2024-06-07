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
import com.michelin.cert.redscan.utils.models.reports.CommonTags;
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import com.michelin.cert.redscan.utils.models.services.HttpService;
import com.michelin.cert.redscan.utils.system.OsCommandExecutor;
import com.michelin.cert.redscan.utils.system.StreamGobbler;

import org.apache.logging.log4j.LogManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 * @author Sylvain VAISSIER
 * @author Maxence SCHMITT
 */
@SpringBootApplication
@EnableScheduling
public class ScanApplication {

  private final RabbitTemplate rabbitTemplate;
  
  @Value("${nuclei.exclude.template.list}")
  private String exclusionList;

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
   * Update nuclei template every day.
   */
  @Scheduled(cron = "@daily")
  public void updateNucleiTemplate() {
    LogManager.getLogger(ScanApplication.class).info("Nuclei : Update template");
    OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
    StreamGobbler streamGobbler = osCommandExecutor.execute("/nucleilauncher");
    if (streamGobbler != null) {
      LogManager.getLogger(ScanApplication.class).info(String.format("Nuclei template update exited with status %s ", streamGobbler.getExitStatus()));
    }
  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_HTTP_SERVICES})
  public void receiveMessage(String message) {
    HttpService httpMessage = new HttpService();
    try {
      httpMessage.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Check default login from : %s", httpMessage.toUrl()));
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      String command = String.format("/nucleilauncher %s http/default-logins/ %s", httpMessage.toUrl(), exclusionList);
      StreamGobbler streamGobbler = osCommandExecutor.execute(command);
      if (streamGobbler != null) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Nuclei exited with status %s ", streamGobbler.getExitStatus()));
        if (streamGobbler.getExitStatus() == 0) {
          JSONArray results = analyzeLines(httpMessage, streamGobbler.getStandardOutputs());
          LogManager.getLogger(ScanApplication.class).info(String.format("Nuclei output for %s : %s ", httpMessage.toUrl(), results.toString()));
          httpMessage.upsertField("nucleidefaultlogins", results);
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Datalake Storage Exception : %s", ex.getMessage()));
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }

  /**
   * Analyze the full nuclei output.
   *
   * @param httpService HTTP Service.
   * @param outputs Nuclei outputs.
   * @return The JSON array corresponding to the output.
   */
  public JSONArray analyzeLines(HttpService httpService, Object[] outputs) {
    JSONArray results = new JSONArray();
    for (Object output : outputs) {
      results.add(analyzeLine(httpService, (String) output));
    }
    return results;
  }

  /**
   * Analyze one line.
   *
   * @param httpService HTTP Service.
   * @param line Line to analyze.
   * @return The output JSON object.
   */
  public JSONObject analyzeLine(HttpService httpService, String line) {
    JSONParser parser = new JSONParser();
    JSONObject jsonResult = null;
    try {
      if (!line.trim().isEmpty()) {
        Object obj = parser.parse(line);
        jsonResult = (JSONObject) obj;

        String url = (String) jsonResult.get("matched-at");
        String templateId = (String) jsonResult.get("template-id");

        //Retrieve template info
        JSONObject info = (JSONObject) jsonResult.get("info");
        String name = (String) info.get("name");
        String severity = (String) info.get("severity");

        //Retrieve extracted results.
        JSONArray extractedResultArray = (JSONArray) jsonResult.get("extracted-results");
        String extractedResult = "";
        if (extractedResultArray != null) {
          extractedResult = String.format(", Extracted values : %s", extractedResultArray.toString());
        }

        LogManager.getLogger(ScanApplication.class).info(String.format("Panel exposure [%s] found  %s", templateId, url));

        raiseVulnerability(convertNucleiSeverity(severity),
                httpService,
                templateId,
                String.format("Default Login %s : %s", httpService.toUrl(), name),
                String.format("A default password is currenty used on this url : %s%s", url, extractedResult));
      }
    } catch (ParseException e) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Error with json line: Parsing %s", e.toString()));
    }
    return jsonResult;
  }

  private int convertNucleiSeverity(String severity) {
    int result = -1;
    switch (severity) {
      case "critical":
        result = Severity.CRITICAL;
        break;
      case "high":
        result = Severity.HIGH;
        break;
      case "low":
        result = Severity.LOW;
        break;
      case "medium":
        result = Severity.MEDIUM;
        break;
      default:
        result = Severity.INFO;
        break;
    }
    return result;
  }

  private void raiseVulnerability(int severity, HttpService service, String vulnName, String title, String message) {
    Vulnerability vuln = new Vulnerability(
            Vulnerability.generateId("redscan-nuclei-defaultlogins", vulnName, service.getDomain(), service.getPort(), service.isSsl() ? "https" : "http"),
            severity,
            title,
            message,
            service.toUrl(),
            "redscan-nuclei-defaultlogins",
            new String[]{CommonTags.EXPOSURE, CommonTags.MISCONFIGURATION}
    );

    rabbitTemplate.convertAndSend(vuln.getFanoutExchangeName(), "", vuln.toJson());
  }

}
