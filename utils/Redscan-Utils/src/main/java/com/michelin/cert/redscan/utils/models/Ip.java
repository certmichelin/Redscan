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

import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.json.JsonUtils;

import kong.unirest.json.JSONObject;

/**
 * Ip model.
 *
 * @author Maxime ESCOURBIAC.
 */
public class Ip extends DatalakeStorageItem implements Sendable {

  private String value;

  /**
   * Default constructor.
   */
  public Ip() {
    index = "ips";
  }

  /**
   * Ip constructor.
   *
   * @param value Ip value.
   */
  public Ip(String value) {
    this();
    this.value = value;
  }

  /**
   * Ip constructor.
   *
   * @param value Ip value.
   * @param parent Ip parent.
   */
  public Ip(String value, String parent) {
    this();
    this.value = value;
    this.parent = parent;
  }

  /**
   * Ip constructor.
   *
   * @param value Ip value.
   * @param parent Ip parent.
   * @param blocked Ip blocking status.
   */
  public Ip(String value, String parent, boolean blocked) {
    this(value, parent);
    setBlocked(blocked);
  }

  @Override
  public String getId() {
    return value;
  }

  /**
   * Get ip adress.
   *
   * @return Ip adress.
   */
  public String getValue() {
    return value;
  }

  /**
   * Set ip adress.
   *
   * @param value Ip adress.
   */
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  protected <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
    return (object == null) ? null
            : (T) new Ip(
                    JsonUtils.getSafeString(object, "value"),
                    JsonUtils.getSafeString(object, "parent"),
                    JsonUtils.getSafeBoolean(object, "blocked"));
  }

  @Override
  public boolean upsert() throws DatalakeStorageException {
    boolean result = (find() != null);
    if (result) {
      result &= this.upsertField("value", value);
      result &= this.upsertField("blocked", getBlocked());
    }
    return result;
  }

  @Override
  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    JsonUtils.setSafeString(jsonObject, "value", value);
    JsonUtils.setSafeString(jsonObject, "parent", parent);
    JsonUtils.setSafeBoolean(jsonObject, "blocked", getBlocked());
    return jsonObject.toString();
  }

  @Override
  public void fromJson(String json) {
    JSONObject jsonObject = new JSONObject(json);
    this.value = JsonUtils.getSafeString(jsonObject, "value");
    this.parent = JsonUtils.getSafeString(jsonObject, "parent");
    setBlocked(JsonUtils.getSafeBoolean(jsonObject, "blocked"));
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.ips";
  }

}
