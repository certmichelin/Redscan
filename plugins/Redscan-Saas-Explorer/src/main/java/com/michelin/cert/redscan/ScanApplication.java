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
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;

import java.net.InetAddress;
import java.net.UnknownHostException;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class ScanApplication {

  private final RabbitTemplate rabbitTemplate;

  /**
   * Constructor to init rabbit template.
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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_BRANDS})
  public void receiveMessage(String message) {
    Brand brand = new Brand();
    try {
      brand.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Start saas discovery : %s", brand.getName()));
      JSONObject result = new JSONObject();
      result.put("slack", checkSlack(brand));
      result.put("atlassian", checkSlack(brand));
      result.put("servicenow", checkServiceNow(brand));
      result.put("sharepoint", checkSharepoint(brand));
      brand.upsertField("saasexplorer", result);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Datalake storage exception : %s", ex.getMessage()));
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception : %s", ex.getMessage()));
    }
  }

  /**
   * Check the existence of slack instance.
   *
   * @param brand brand to check.
   * @return True if slack instance was discovered.
   */
  public boolean checkSlack(Brand brand) {
    boolean result = false;
    try {
      LogManager.getLogger(ScanApplication.class).info(String.format("Start slack : %s", brand.getName()));
      try {
        HttpResponse<JsonNode> jsonResponse = Unirest.get(String.format("https://%s.slack.com", brand.getName())).asJson();
        LogManager.getLogger(ScanApplication.class).info(String.format("Slack response code for %s : %d", brand.getName(), jsonResponse.getStatus()));

        //Send the alert.
        if (jsonResponse.getStatus() == 200) {
          Vulnerability vulnerability = new Vulnerability(
                  Vulnerability.generateId("redscan-saas-explorer", "slack", brand.getName()),
                  Severity.INFO,
                  String.format("[%s] Slack instance found", brand.getName()),
                  String.format("A slack instance has been found : %s", brand.getName()),
                  String.format("https://%s.slack.com", brand.getName()),
                  "redscan-saas-explorer",
                  new String[]{"footprint"}
          );
          rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());
          result = true;
        }
      } catch (UnirestException ex) {
        LogManager.getLogger(ScanApplication.class).error(String.format("Unirest Exception : %s", ex.getMessage()));
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception for slack research : %s", ex.getMessage()));
    }
    return result;
  }

  /**
   * Check the existence of Atlassian cloud instance.
   *
   * @param brand brand to check.
   * @return True if atlassian instance was discovered.
   */
  public boolean checkAtlassian(Brand brand) {
    boolean result = false;
    try {
      LogManager.getLogger(ScanApplication.class).info(String.format("Start atlassian : %s", brand.getName()));
      try {
        HttpResponse<String> response = Unirest.get(String.format("https://%s.atlassian.net", brand.getName())).asString();
        LogManager.getLogger(ScanApplication.class).info(String.format("Atlassian response code for %s : %d", brand.getName(), response.getStatus()));

        //Send the alert.
        if (response.getStatus() != 404) {
          Vulnerability vulnerability = new Vulnerability(
                  Vulnerability.generateId("redscan-saas-explorer", "atlassian", brand.getName()),
                  Severity.INFO,
                  String.format("[%s] Atlassian instance found", brand.getName()),
                  String.format("An atlassian instance has been found : %s", brand.getName()),
                  String.format("https://%s.atlassian.net", brand.getName()),
                  "redscan-saas-explorer",
                  new String[]{"footprint"}
          );
          rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());
          result = true;
        }
      } catch (UnirestException ex) {
        LogManager.getLogger(ScanApplication.class).error(String.format("Unirest Exception : %s", ex.getMessage()));
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception for atlassian research : %s", ex.getMessage()));
    }
    return result;
  }

  /**
   * Check the existence of service-now cloud instance.
   *
   * @param brand brand to check.
   * @return True if service-now instance was discovered.
   */
  public boolean checkServiceNow(Brand brand) {
    boolean result = false;
    try {
      LogManager.getLogger(ScanApplication.class).info(String.format("Start service now : %s", brand.getName()));
      try {
        InetAddress.getByName(String.format("%s.service-now.com", brand.getName()));
        Vulnerability vulnerability = new Vulnerability(
                Vulnerability.generateId("redscan-saas-explorer", "service-now", brand.getName()),
                Severity.INFO,
                String.format("[%s] ServiceNow instance found", brand.getName()),
                String.format("A ServiceNow instance has been found : %s", brand.getName()),
                String.format("https://%s.service-now.com", brand.getName()),
                "redscan-saas-explorer",
                new String[]{"footprint"}
        );
        rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());
        result = true;
      } catch (UnknownHostException ex) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Service-Now domain not found : %s", String.format("%s.service-now.com", brand.getName())));
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception for service-now research : %s", ex.getMessage()));
    }
    return result;
  }

  /**
   * Check the existence of sharepoint cloud instance.
   *
   * @param brand brand to check.
   * @return True if sharepoint instance was discovered.
   */
  public boolean checkSharepoint(Brand brand) {
    boolean result = false;
    try {
      LogManager.getLogger(ScanApplication.class).info(String.format("Start sharepoint : %s", brand.getName()));
      try {
        InetAddress.getByName(String.format("%s.sharepoint.com", brand.getName()));
        Vulnerability vulnerability = new Vulnerability(
                Vulnerability.generateId("redscan-saas-explorer", "sharepoint", brand.getName()),
                Severity.INFO,
                String.format("[%s] Sharepoint instance found", brand.getName()),
                String.format("A Sharepoint instance has been found : %s", brand.getName()),
                String.format("https://%s.sharepoint.com", brand.getName()),
                "redscan-saas-explorer",
                new String[]{"footprint"}
        );
        rabbitTemplate.convertAndSend(vulnerability.getFanoutExchangeName(), "", vulnerability.toJson());
        result = true;
      } catch (UnknownHostException ex) {
        LogManager.getLogger(ScanApplication.class).info(String.format("Sharepoint domain not found : %s", String.format("%s.sharepoint.com", brand.getName())));
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception for sharepoint research : %s", ex.getMessage()));
    }
    return result;
  }

}
