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

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


/**
 * Mail Configuration class.
 *
 * @author Axel REMACK
 */
@Configuration
public class MailConfig {
  @Value("${mail.recipients}")
  private String[] to;

  @Value("${mail.host}")
  private String host;
  
  @Value("${mail.port}")
  private String port = "587";
  
  @Value("${mail.sender}")
  private String from;

  @Value("${mail.password}")
  private String mailAppPassword;
  
  private Properties properties;


  /**
   * Default constructor.
   */
  public MailConfig() {
    this.properties = System.getProperties();
    this.properties.setProperty("mail.smtp.port", this.port);
  }

  /**
   * Constructor specifying recipients.
   */
  public MailConfig(String[] to) {
    this();
    this.to = to;
  }

  /**
   * Getters and setters.
   */
  public String[] getTo() {
    return to;
  }

  public void setTo(String[] to) {
    this.to = to;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getMailAppPassword() {
    return mailAppPassword;
  }

  public void setMailAppPassword(String mailApikey) {
    this.mailAppPassword = mailApikey;
  }

  /**
   * Java Mail Sender configuration method.
   *
   * @return JavaMailSenderImpl.
   */
  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(this.host);
    mailSender.setPort(Integer.parseInt(this.port));

    mailSender.setUsername(this.from);
    mailSender.setPassword(this.mailAppPassword);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "true");

    return mailSender;
  }
}

