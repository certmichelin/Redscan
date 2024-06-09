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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan Wappalyzer main class.
 *
 * @author Florent BORDIGNON
 * @author Sylvain Vaissier
 * @author Maxime Escourbiac
 */
@SpringBootApplication
public class WappalyzerScanApplication {

  @Autowired
  private CacheConfig cacheConfig;

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(WappalyzerScanApplication.class, args);
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
      LogManager.getLogger(WappalyzerScanApplication.class).info(String.format("Start Wappalyzer service : %s:%s (ssl : %s)", httpService.getDomain(), httpService.getPort(), httpService.isSsl()));

      //Check if the information is in cache. The cache validity is 24 hours for redscan-wappalyzer.
      //The cache is used here to avoid to scan the same web applications several time a day.
      Map<String, String> cache = cacheConfig.getCache(cacheConfig.buildKey(httpService.getDomain(), httpService.getPort(), Boolean.toString(httpService.isSsl())), 24);
      if (cache == null) {
        try {
          String wappalyzerCmd = String.format("/usr/local/bin/wappalyzer %s", httpService.toUrl());
          OsCommandExecutor osCommandExecutor = new OsCommandExecutor();
          StreamGobbler streamGobbler = osCommandExecutor.execute(wappalyzerCmd);
          if (streamGobbler != null) {
            LogManager.getLogger(WappalyzerScanApplication.class).info(String.format("Wappalyzer terminated with status : %d", streamGobbler.getExitStatus()));

            Object[] stdoutObjLines = streamGobbler.getStandardOutputs();
            if (stdoutObjLines != null) {
              JSONObject wappalyzerJson = new JSONObject(String.join("\n", (Arrays.copyOf(stdoutObjLines, stdoutObjLines.length, String[].class))));
              if (!wappalyzerJson.isNull("technologies")) {
                JSONArray technologies = wappalyzerJson.getJSONArray("technologies");
                LogManager.getLogger(WappalyzerScanApplication.class).info(String.format("Wappalyzer output for %s://%s:%s : %s", (httpService.isSsl()) ? "https" : "http", httpService.getDomain(), httpService.getPort(), technologies.toString()));
                httpService.upsertField("wappalyzer", technologies);
                cacheConfig.setCache(cacheConfig.buildKey(httpService.getDomain(), httpService.getPort(), Boolean.toString(httpService.isSsl())), new HashMap<>());
              }
            }
          }
        } catch (DatalakeStorageException ex) {
          LogManager.getLogger(WappalyzerScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex.getMessage()));
        } catch (Exception ex) {
          LogManager.getLogger(WappalyzerScanApplication.class).error(String.format("Exception : %s", ex.getMessage()));
        }
      }
    } catch (Exception ex) {
      LogManager.getLogger(WappalyzerScanApplication.class).error(String.format("General exception : %s", ex.getMessage()));
    }
  }
}
