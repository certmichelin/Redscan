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

package com.michelin.cert.redscan.utils.models.reports;

/**
 * Common Alert model.
 *
 * @author Maxime ESCOURBIAC
 */
public class Alert extends Vulnerability {

  /**
   * Default constructor.
   */
  public Alert() {
  }

  /**
   * Create an alert from vulnerability.
   *
   * @param vulnerability Vulnerability model.
   */
  public Alert(Vulnerability vulnerability) {
    super(vulnerability.getId(),
            vulnerability.getSeverity(),
            vulnerability.getSummary(),
            vulnerability.getDescription(),
            vulnerability.getUrl(),
            vulnerability.getOrigin(),
            vulnerability.getTags());
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.alerts";
  }

}
