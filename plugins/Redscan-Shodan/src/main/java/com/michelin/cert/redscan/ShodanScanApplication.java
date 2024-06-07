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
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.models.Domain;
import com.michelin.cert.redscan.utils.models.Ip;
import com.michelin.cert.redscan.utils.models.services.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan Subfinder main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class ShodanScanApplication {

  private final RabbitTemplate rabbitTemplate;

  @Autowired
  private CacheConfig cacheConfig;

  @Value("${shodan.api.key}")
  private String apiKey;

  /**
   * Constructor to init rabbit template.
   *
   * @param rabbitTemplate Rabbit template.
   */
  public ShodanScanApplication(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(ShodanScanApplication.class, args);
  }

  /**
   * Domain message executor
   *
   * @param message Domain received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_DOMAINS})
  public void receiveDomainMessage(String message) {
    Domain domain = new Domain();
    try {
      domain.fromJson(message);

      LogManager.getLogger(ShodanScanApplication.class).info(String.format("Start shodan : %s", domain.getName()));

      InetAddress addr = java.net.InetAddress.getByName(domain.getName());
      String ip = addr.getHostAddress();

      executeShodanRequest(ip, domain);
    } catch (UnknownHostException ex) {
      LogManager.getLogger(ShodanScanApplication.class).info(String.format("IP not found : %s", domain.getName()));
    } catch (Exception ex) {
      LogManager.getLogger(ShodanScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }

  /**
   * Ip message executor
   *
   * @param message Domain received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_IPS})
  public void receiveIpMessage(String message) {
    Ip ip = new Ip();
    try {
      ip.fromJson(message);
      LogManager.getLogger(ShodanScanApplication.class).info(String.format("Start shodan : %s", ip.getValue()));
      executeShodanRequest(ip.getValue(), ip);
    } catch (Exception ex) {
      LogManager.getLogger(ShodanScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }

  private void executeShodanRequest(String ip, DatalakeStorageItem datalakeStorageItem) {
    try {
      //Check if the information is in cache. The cache validity is 24 hours for shodan.
      Map<String, String> cache = cacheConfig.getCache(cacheConfig.buildKey(ip), 24);

      if (cache == null) {
        TimeUnit.SECONDS.sleep(2); //Sleep for 2s to respect the API time limit restriction.
        HttpResponse<JsonNode> jsonResponse = Unirest.get(String.format("https://api.shodan.io/shodan/host/%s?key=%s", ip.trim(), apiKey)).asJson();
        if (jsonResponse.getStatus() == 200) {
          LogManager.getLogger(ShodanScanApplication.class).info(String.format("Found in shodan %s (%s) : %s", datalakeStorageItem.getId(), ip, jsonResponse.getBody().toString()));
          handleShodanResponse(datalakeStorageItem, ip, jsonResponse.getBody().getObject());

          //Cache the response
          cache = new HashMap<>();
          cache.put("json", jsonResponse.getBody().getObject().toString());
          cacheConfig.setCache(cacheConfig.buildKey(ip), cache);
        } else {
          LogManager.getLogger(ShodanScanApplication.class).info(String.format("Not found in shodan %s (%s) : %s", datalakeStorageItem.getId(), ip, jsonResponse.getBody().toString()));
        }
      } else {
        LogManager.getLogger(ShodanScanApplication.class).info(String.format("Found in shodan cache %s (%s) : %s", datalakeStorageItem.getId(), ip, cache.get("json")));
        handleShodanResponse(datalakeStorageItem, ip, new JSONObject(cache.get("json")));
      }

    } catch (UnirestException | InterruptedException ex) {
      LogManager.getLogger(ShodanScanApplication.class).error(String.format("Exception : %s", ex.getMessage()));
    } catch (JSONException ex) {
      LogManager.getLogger(ShodanScanApplication.class).error(String.format("Failed to parse shodan result : %s", ex.getMessage()));
    } catch (Exception ex) {
      LogManager.getLogger(ShodanScanApplication.class).error(String.format("General Exception : %s", ex.getMessage()));
    }
  }

  /**
   * Handle Shodan response.
   *
   * @param domain Domain to manage
   * @param ip Ip to manage.
   * @param shodanResultJson Json object from Shodan.
   */
  private void handleShodanResponse(DatalakeStorageItem datalakeStorageItem, String ip, JSONObject shodanResultJson) {
    //Extract port from shodan in order to publish into SERVICES queue.
    JSONArray ports = (JSONArray) (shodanResultJson.get("ports"));
    for (int i = 0; i < ports.length(); i++) {
      //Craft the service and send to SERVICE queue.
      Service service = new Service(datalakeStorageItem.getId(), ip, ports.getString(i));
      rabbitTemplate.convertAndSend(service.getFanoutExchangeName(), "", service.toJson());
    }
    try {
      datalakeStorageItem.upsertField("shodan", shodanResultJson);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(ShodanScanApplication.class).error(String.format("DatalakeStorage exception : %s", ex.getMessage()));
    }
  }

}
