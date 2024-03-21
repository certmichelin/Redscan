/*
 * Copyright 2023 Michelin CERT (https://cert.michelin.com/)
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

package com.michelin.cert.redscan.config;

import com.michelin.cert.redscan.utils.mantisbt.MantisBtClient;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configure MantisBt client.
 *
 * @author Maxime ESCOURBIAC
 */
@Configuration
public class MantisBtConfig {

  @Value("${mantisbt.url}")
  private String mantisBtUrl;
  
  @Value("${mantisbt.username}")
  private String mantisBtUsername;
  
  @Value("${mantisbt.password}")
  private String mantisBtPassword;

  @Autowired
  public MantisBtConfig() {
  }

  @PostConstruct
  public void initMantisBtClient() {
    MantisBtClient.init(mantisBtUrl, mantisBtUsername, mantisBtPassword);
  }

}
