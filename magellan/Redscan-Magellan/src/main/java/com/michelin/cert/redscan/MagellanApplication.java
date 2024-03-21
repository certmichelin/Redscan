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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
@EnableScheduling
public class MagellanApplication {

  /**
   * Magellan main method.
   *
   * @param args App argument.
   * @throws Exception Exception during the run.
   */
  public static void main(String[] args) throws Exception {
    SpringApplication.run(MagellanApplication.class, args);
  }

}
