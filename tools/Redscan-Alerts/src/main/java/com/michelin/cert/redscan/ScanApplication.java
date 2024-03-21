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

import com.michelin.cert.redscan.utils.models.reports.Alert;
import com.michelin.cert.redscan.utils.models.reports.Severity;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class ScanApplication {

  @Autowired
  TeamsService teamsService;

  @Autowired
  MailService mailService;

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
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_ALERTS})
  public void receiveMessage(String message) {
    
    try {
      Alert alert = new Alert();
      alert.fromJson(message);
      LogManager.getLogger(ScanApplication.class).info(String.format("Received Alert : %s", alert.toJson()));
      
      switch (alert.getSeverity()) {
        case Severity.CRITICAL:
          teamsService.sendMessage(alert);
          mailService.sendMessage(alert);
          break;
        case Severity.HIGH:
          teamsService.sendMessage(alert);
          mailService.sendMessage(alert);
          break;
        case Severity.MEDIUM:
          teamsService.sendMessage(alert);
          mailService.sendMessage(alert);
          break;
        case Severity.LOW:
          //Not used for the moment.
          break;
        case Severity.INFO:
          //Not used for moment.
          break;
        default:
          break;
      }
    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Exception : %s", ex.getMessage()));
    }

  }

}
