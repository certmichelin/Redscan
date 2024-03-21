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

package com.michelin.cert.redscan.utils.models;

/**
 * Service levels enumeration.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */
public enum ServiceLevel {
  GOLD(1),
  SILVER(2),
  BRONZE(3),
  NONE(4);

  private final int value;

  private ServiceLevel(int value) {
    this.value = value;
  }

  /**
   * Retrieve ServiceLevel instance value.
   *
   * @return ServiceLevel instance value
   */
  public int getValue() {
    return this.value;
  }

  /**
   * Find ServiceLevel by int value.
   *
   * @param value Int value of ServiceLevel to find
   * @return ServiceLevel instance with given value
   */
  public static ServiceLevel findByValue(int value) {
    ServiceLevel result = null;

    for (ServiceLevel serviceLevel : ServiceLevel.values()) {
      if (serviceLevel.getValue() == value) {
        result = serviceLevel;
        break;
      }
    }

    return result;
  }

}
