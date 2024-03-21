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
 * Domain model.
 *
 * @author Maxime ESCOURBIAC.
 * @author Axel REMACK.
 */
public class Domain extends DatalakeStorageItem implements Sendable {

  private String name;

  /**
   * Default constructor.
   */
  public Domain() {
    index = "domains";
  }

  /**
   * Domain constructor.
   *
   * @param name Domain name.
   */
  public Domain(String name) {
    this();
    this.name = name;
  }

  /**
   * Domain constructor.
   *
   * @param name Domain name.
   * @param parent Domain parent.
   */
  public Domain(String name, String parent) {
    this();
    this.name = name;
    this.parent = parent;
  }

  /**
   * Domain constructor.
   *
   * @param name Domain name.
   * @param parent Domain parent.
   * @param blocked Domain blocking status.
   */
  public Domain(String name, String parent, boolean blocked) {
    this(name, parent);
    setBlocked(blocked);
  }

  @Override
  public String getId() {
    return name;
  }

  /**
   * Domain name.
   *
   * @return Domain name.
   */
  public String getName() {
    return name;
  }

  /**
   * Domain name.
   *
   * @param name Domain name.
   */
  public void setName(String name) {
    this.name = name;
  }


  @Override
  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    JsonUtils.setSafeString(jsonObject, "name", name);
    JsonUtils.setSafeString(jsonObject, "parent", parent);
    JsonUtils.setSafeBoolean(jsonObject, "blocked", getBlocked());
    return jsonObject.toString();
  }

  @Override
  public void fromJson(String json) {
    JSONObject jsonObject = new JSONObject(json);
    this.name = JsonUtils.getSafeString(jsonObject, "name");
    this.parent = JsonUtils.getSafeString(jsonObject, "parent");
    setBlocked(JsonUtils.getSafeBoolean(jsonObject, "blocked"));
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.domains";
  }

  @Override
  public <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
    return (object == null) ? null
            : (T) new Domain(
                    JsonUtils.getSafeString(object, "name"),
                    JsonUtils.getSafeString(object, "parent"),
                    JsonUtils.getSafeBoolean(object, "blocked"));
  }

  @Override
  public boolean upsert() throws DatalakeStorageException {
    boolean result = (find() != null);
    if (result) {
      result &= this.upsertField("name", name);
      result &= this.upsertField("blocked", getBlocked());
    }
    return result;
  }
}
