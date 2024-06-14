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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import kong.unirest.UnirestException;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

/**
 * Mail service.
 *
 * @author Axel REMACK
 */
@Service
public class MailService {

  @Autowired
  MailConfig mailConfig;

  @Autowired
  private JavaMailSender emailSender;

  /**
   * Default constructor.
   */
  public MailService() {
  }

  /**
   * Send a message via email.
   *
   * @param alert Alert to manage.
   * @return True if the message has been sent.
   */
  public boolean sendMessage(Alert alert) {
    boolean result = false;
    try {
      LogManager.getLogger(MailService.class).info(String.format("Send message to mail : %s", alert.getSummary()));

      //Test if the SMTP host is defined.
      if (mailConfig.getHost() != null && !mailConfig.getHost().trim().isEmpty()) {
        
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(mailConfig.getFrom());
        helper.setTo(mailConfig.getTo());
        String subject = String.format("[P%d] Redscan alert - %s", alert.getSeverity(), alert.getSummary());
        helper.setSubject(subject);
        String content = String.format("<h3>P%d - %s</h3>", alert.getSeverity(),
            HtmlUtils.htmlEscape(alert.getSummary()))
            + String.format("<p><b>Description</b>: %s</p>", HtmlUtils.htmlEscape(alert.getDescription()))
            + "<br/>This message was system generated. Do not reply to this message.";
        helper.setText(content, true);
        emailSender.send(message);

        result = true;
      } else {
        LogManager.getLogger(MailService.class).info("SMTP host is not defined. Message was not sent.");
      }
    } catch (UnirestException ex) {
      LogManager.getLogger(MailService.class).error(ex);
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }

    return result;
  }
}
