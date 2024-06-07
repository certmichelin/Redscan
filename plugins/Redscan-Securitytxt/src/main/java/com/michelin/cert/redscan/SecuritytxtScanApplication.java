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

import javax.annotation.PostConstruct;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

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
public class SecuritytxtScanApplication {

  /**
   * Initialize Unirest options.
   */
  @PostConstruct
  public void configureUnirest() {
    Unirest.config().verifySsl(false);
    Unirest.config().followRedirects(false);
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(SecuritytxtScanApplication.class, args);
  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_HTTP_SERVICES})
  public void receiveMessage(String message) {
    HttpService serviceMessage = new HttpService();
    try {
      serviceMessage.fromJson(message);
      LogManager.getLogger(SecuritytxtScanApplication.class).info(String.format("Checking Security text on : %s", serviceMessage.toUrl()));
      try {
        String url = String.format("%s/.well-known/security.txt", serviceMessage.toUrl());
        String result = retrieveSecurityTxtContent(url);
        if (result == null) {
          url = String.format("%s/security.txt", serviceMessage.toUrl());
          result = retrieveSecurityTxtContent(url);
        }

        if (result != null) {
          LogManager.getLogger(SecuritytxtScanApplication.class).info(String.format("Found security.txt at %s", url));
          serviceMessage.upsertField("securitytxt", result);
        } else {
          LogManager.getLogger(SecuritytxtScanApplication.class).info(String.format("Security text not found %s", serviceMessage.toUrl()));
          serviceMessage.upsertField("securitytxt", "not found");
        }
      } catch (DatalakeStorageException ex) {
        LogManager.getLogger(SecuritytxtScanApplication.class).error(String.format("Datalake Strorage exception : %s", ex));
      } catch (Exception ex) {
        LogManager.getLogger(SecuritytxtScanApplication.class).error(String.format("Exception : %s", ex));
      }
    } catch (Exception ex) {
      LogManager.getLogger(SecuritytxtScanApplication.class).error(String.format("General Exception : %s", ex));
    }
  }

  private String retrieveSecurityTxtContent(String url) {
    String result = null;
    LogManager.getLogger(SecuritytxtScanApplication.class).info(String.format("Checking Security txt on : %s", url));
    HttpResponse<String> response = Unirest.get(url).asString();
    if (response.getStatus() == 200) {
      LogManager.getLogger(SecuritytxtScanApplication.class).info(String.format("URL (%s) return 200 response code", url));
      if (response.getBody() != null && response.getBody().contains("Contact:")) {
        result = response.getBody();
      } else {
        LogManager.getLogger(SecuritytxtScanApplication.class).info(String.format("URL (%s) seems not be a real security txt file", url));
      }
    } else {
      LogManager.getLogger(SecuritytxtScanApplication.class).info(String.format("URL (%s) return %s response code", url, response.getStatus()));
    }
    return result;
  }

}
