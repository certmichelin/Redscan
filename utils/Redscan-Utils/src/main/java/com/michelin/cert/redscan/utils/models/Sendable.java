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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Sendable interface. That be used by models to be sent over MQ.
 *
 * @author Maxime ESCOURBIAC
 */
public interface Sendable {

  /**
   * Convert the object to json string.
   *
   * @return The string representation of the json object.
   */
  public abstract String toJson();

  /**
   * Convert json string to object.
   *
   * @param json The string representation of the json object.
   */
  public abstract void fromJson(String json);

  /**
   * Get Fanout exchange name.
   *
   * @return Fanout exchange name.
   */
  @JsonIgnore
  public abstract String getFanoutExchangeName();

}
