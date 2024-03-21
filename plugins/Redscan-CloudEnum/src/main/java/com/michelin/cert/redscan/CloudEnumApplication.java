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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

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
 *
 */
@SpringBootApplication
public class CloudEnumApplication {

  private final RabbitTemplate rabbitTemplate;

  /**
   * Constructor to init rabbit template. Only required if pushing data to
   * queues
   *
   * @param rabbitTemplate Rabbit template.
   */
  public CloudEnumApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(CloudEnumApplication.class, args);
  }

  /**
   * Vulnerabilities creator.
   *
   * @param message The message received aka brand.
   * @param name The name of the vuln eg:Firebase,openGCp.
   * @param url Url open.
   */
  public void createVuln(String message, String name, String url) {

    Vulnerability vulnerability = new Vulnerability(
            Vulnerability.generateId("redscan-cloudenum", message, name, url),
            Severity.MEDIUM,
            String.format("[%s] OPEN %s : %s", message, name, url),
            String.format("This bucket %s is potentially accessible : %s", message, url),
            url,
            "redscan-cloudenum",
            new String[]{CommonTags.EXPOSURE, CommonTags.MISCONFIGURATION}
    );

    rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());

  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_BRANDS})
  public void receiveMessage(String message) {
    Brand brand = new Brand();
    try {
      brand.fromJson(message);
      String filename = UUID.randomUUID().toString().replace("-", "");
      OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
      LogManager.getLogger(CloudEnumApplication.class).info(String.format("Starting Cloud Enum : %s", brand.getName()));
      StreamGobbler streamGobbler = osCommandExecutor.execute(String.format("python3 /usr/bin/cloud_enum/cloud_enum.py -k %s -j /usr/share/%soutput.json", brand.getName(), filename));

      if (streamGobbler != null) {
        LogManager.getLogger(CloudEnumApplication.class).info(String.format("Cloud Enum exited with status %s ", streamGobbler.getExitStatus()));
        if (streamGobbler.getErrorOutputs().length != 0) {
          for (Object object : streamGobbler.getErrorOutputs()) {
            String result = (String) object;
            LogManager.getLogger(CloudEnumApplication.class).info(String.format("Cloud Enum Output: %s", result));
          }
        }

      } else {
        LogManager.getLogger(CloudEnumApplication.class).error(String.format("Error launching script"));
      }

      JSONParser parser = new JSONParser();
      try {

        Object obj = parser.parse(new FileReader(String.format("/usr/share/%soutput.json", filename)));
        JSONObject jsonResult = (JSONObject) obj;
        LogManager.getLogger(CloudEnumApplication.class).info(String.format("JSONObject is %s", jsonResult));
        brand.upsertField("CloudEnum", jsonResult);

        //Getting Open GCP Bucket
        JSONObject gcpObj = (JSONObject) jsonResult.get("gcp");
        JSONObject bucketObj = (JSONObject) gcpObj.get("bucket");
        JSONArray openGcp = (JSONArray) bucketObj.get("open");
        if (!openGcp.isEmpty()) {
          for (int i = 0; i < openGcp.size(); ++i) {
            createVuln(brand.getName(), "OpenGCP", openGcp.get(i).toString());
          }

        }

        //Getting Open GCP function on post and get
        JSONObject functionObj = (JSONObject) gcpObj.get("function");
        JSONObject openFunction = (JSONObject) functionObj.get("open");
        JSONArray openPost = (JSONArray) openFunction.get("post");
        JSONArray openGet = (JSONArray) openFunction.get("get");
        if (!openPost.isEmpty()) {
          for (int i = 0; i < openPost.size(); ++i) {
            createVuln(brand.getName(), "openPost", openPost.get(i).toString());
          }
        }
        if (!openGet.isEmpty()) {
          for (int i = 0; i < openGet.size(); ++i) {
            createVuln(brand.getName(), "openGet", openGet.get(i).toString());
          }

        }

        //Getting Open GCP appspot
        JSONObject appspotObj = (JSONObject) gcpObj.get("appspot");
        JSONArray openApp = (JSONArray) appspotObj.get("open");
        if (!openApp.isEmpty()) {
          for (int i = 0; i < openApp.size(); ++i) {
            createVuln(brand.getName(), "openApp", openApp.get(i).toString());
          }
        }

        //Getting Open GCP firebase
        JSONObject firebaseObj = (JSONObject) gcpObj.get("firebase");
        JSONArray openFirebase = (JSONArray) firebaseObj.get("open");
        if (!openFirebase.isEmpty()) {
          for (int i = 0; i < openFirebase.size(); ++i) {
            createVuln(brand.getName(), "openFirebase", openFirebase.get(i).toString());
          }
        }

        //Getting Open aws bucket
        JSONObject awsObj = (JSONObject) jsonResult.get("aws");
        JSONObject s3Obj = (JSONObject) awsObj.get("s3");
        JSONArray openAws = (JSONArray) s3Obj.get("open");
        if (!openAws.isEmpty()) {
          for (int i = 0; i < openAws.size(); ++i) {
            createVuln(brand.getName(), "openAws", openAws.get(i).toString());
          }
        }

        File file = new File(String.format("/usr/share/%soutput.json", filename));
        if (file.delete()) {
          LogManager.getLogger(CloudEnumApplication.class).info(String.format("Temp file has been suppressed "));
        } else {
          LogManager.getLogger(CloudEnumApplication.class).warn(String.format("Temp file has not been deleted. Expect garbadge on disk"));
        }

      } catch (FileNotFoundException e) {
        LogManager.getLogger(CloudEnumApplication.class).error(String.format("Error with json file: not found : %s", e.toString()));
      } catch (IOException e) {
        LogManager.getLogger(CloudEnumApplication.class).error(String.format("Error with json file: IO : %S", e.toString()));
      } catch (ParseException e) {
        LogManager.getLogger(CloudEnumApplication.class).error(String.format("Error with json file: Parsing %s", e.toString()));
      } catch (DatalakeStorageException e) {
        LogManager.getLogger(CloudEnumApplication.class).error(String.format("DATALAKE ERROR: %s", e.toString()));
      }
    } catch (Exception e) {
      LogManager.getLogger(CloudEnumApplication.class).error(String.format("General exception: %s", e.toString()));
    }
  }

}
