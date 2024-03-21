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

import java.io.Serializable;
import java.util.Date;

import kong.unirest.json.JSONObject;

/**
 * Brand model.
 *
 * @author Maxime ESCOURBIAC.
 */
public class Brand extends DatalakeStorageItem implements Sendable, Serializable {

  private String name;
  private int serviceLevel;

  /**
   * Default constructor.
   */
  public Brand() {
    index = "brands";
    parent = null;
  }

  /**
   * Brand constructor.
   *
   * @param name Brand name.
   */
  public Brand(String name) {
    this();
    this.name = name;
  }

  /**
   * Brand constructor.
   *
   * @param name Brand name.
   * @param serviceLevel Brand service level.
   * @param lastScanDate Brand last scan date.
   */
  public Brand(String name, int serviceLevel, Date lastScanDate) {
    this(name);
    this.serviceLevel = serviceLevel;
    setLastScanDate(lastScanDate);
  }

  @Override
  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    JsonUtils.setSafeString(jsonObject, "name", name);
    JsonUtils.setSafeInt(jsonObject, "serviceLevel", serviceLevel);
    return jsonObject.toString();
  }

  @Override
  public void fromJson(String json) {
    JSONObject jsonObject = new JSONObject(json);
    this.name = JsonUtils.getSafeString(jsonObject, "name");
    this.serviceLevel = JsonUtils.getSafeInt(jsonObject, "serviceLevel");
  }

  @Override
  public <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
    return (object == null) ? null
            : (T) new Brand(
                    JsonUtils.getSafeString(object, "name"),
                    JsonUtils.getSafeInt(object, "serviceLevel"),
                    toDate(JsonUtils.getSafeString(object, "last_scan_date")));
  }

  @Override
  public boolean upsert() throws DatalakeStorageException {
    boolean result = (find() != null);
    if (result) {
      result &= this.upsertField("name", name);
      result &= this.upsertField("serviceLevel", serviceLevel);
      result &= this.upsertField("last_scan_date", fromDate(getLastScanDate()));
    }
    return result;
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.brands";
  }

  @Override
  public String getId() {
    return name;
  }

  /**
   * Brand name.
   *
   * @return Brand name.
   */
  public String getName() {
    return name;
  }

  /**
   * Brand name.
   *
   * @param name Brand name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Brand service level.
   *
   * @return Brand service level.
   */
  public int getServiceLevel() {
    return serviceLevel;
  }

  /**
   * Brand service level.
   *
   * @param serviceLevel Brand service level.
   */
  public void setServiceLevel(int serviceLevel) {
    this.serviceLevel = serviceLevel;
  }

}
