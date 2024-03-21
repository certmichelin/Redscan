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

import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Teams service.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */
@Service
public class TeamsService {

  @Autowired
  TeamsConfig teamsConfig;

  /**
   * Default constructor.
   */
  public TeamsService() {
  }

  /**
   * Send a message on Teams.
   *
   * @param alert Alert to manage.
   * @return True if the message has been sent.
   */
  public boolean sendMessage(Alert alert) {
    boolean result = false;
    try {
      LogManager.getLogger(TeamsService.class).info(String.format("Send message to team : %s", alert.getSummary()));
      JSONObject message = new JSONObject();
      message.put("@type", "MessageCard");
      message.put("summary", alert.getSummary());
      JSONObject sectionInfos = new JSONObject();
      sectionInfos.put("activityTitle", String.format("<h1><b>%s</b></h1>", alert.getSummary()));
      sectionInfos.put("activitySubtitle", alert.getDescription());
      JSONArray facts = new JSONArray();
      JSONObject priority = new JSONObject();
      priority.put("name", "<p style=\"margin-left: 25px;\">Priority</p>");
      priority.put("value", alert.getSeverity());
      facts.put(priority);
      JSONObject url = new JSONObject();
      url.put("name", "<p style=\"margin-left: 25px;\">URL</p>");
      url.put("value", alert.getUrl());
      facts.put(url);
      sectionInfos.put("facts", facts);
      JSONArray sections = new JSONArray();
      sections.put(sectionInfos);
      message.put("sections", sections);

      result = Unirest.post(teamsConfig.getWebhookUrl()).body(message).asString().getStatus() == 200;
      LogManager.getLogger(TeamsService.class).info(String.format("Send Message to teams : Success = %s", result));
    } catch (UnirestException ex) {
      LogManager.getLogger(TeamsService.class).error(ex);
    }
    return result;
  }
}
