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

import java.util.Date;

import kong.unirest.json.JSONObject;

/**
 * MasterDomain model.
 *
 * @author Maxime ESCOURBIAC.
 */
public class MasterDomain extends DatalakeStorageItem implements Sendable {

  private String name;
  private int serviceLevel;
  private boolean inScope;
  private boolean reviewed;

  /**
   * Default constructor.
   */
  public MasterDomain() {
    index = "master_domains";
  }

  /**
   * MasterDomain constructor.
   *
   * @param name MasterDomain name.
   */
  public MasterDomain(String name) {
    this();
    this.name = name;
  }

  /**
   * MasterDomain constructor.
   *
   * @param name MasterDomain name.
   * @param parent MasterDomain parent.
   */
  public MasterDomain(String name, String parent) {
    this();
    this.name = name;
    this.parent = parent;
  }

  /**
   * MasterDomain constructor.
   *
   * @param name MasterDomain name.
   * @param serviceLevel MasterDomain service level.
   * @param inScope MasterDomain isInScope.
   * @param reviewed MasterDomain isReviewed.
   * @param lastScanDate Last scan date.
   * @param parent MasterDomain parent.
   */
  public MasterDomain(String name, int serviceLevel, boolean inScope, boolean reviewed, Date lastScanDate, String parent) {
    this(name, parent);
    this.serviceLevel = serviceLevel;
    this.inScope = inScope;
    this.reviewed = reviewed;
    setLastScanDate(lastScanDate);
  }

  @Override
  public String getId() {
    return name;
  }

  /**
   * Master domain name.
   *
   * @return Master domain name.
   */
  public String getName() {
    return name;
  }

  /**
   * Master domain name.
   *
   * @param name Master domain name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * MasterDomain isInScope.
   *
   * @return MasterDomain isInScope.
   */
  public boolean isInScope() {
    return inScope;
  }

  /**
   * MasterDomain isInScope.
   *
   * @param inScope MasterDomain isInScope.
   */
  public void setInScope(boolean inScope) {
    this.inScope = inScope;
  }

  /**
   * MasterDomain isReviewed.
   *
   * @return MasterDomain isReviewed.
   */
  public boolean isReviewed() {
    return reviewed;
  }

  /**
   * MasterDomain isReviewed.
   *
   * @param reviewed MasterDomain isReviewed.
   */
  public void setReviewed(boolean reviewed) {
    this.reviewed = reviewed;
  }

  /**
   * MasterDomain service level.
   *
   * @return MasterDomain service level.
   */
  public int getServiceLevel() {
    return serviceLevel;
  }

  /**
   * MasterDomain service level.
   *
   * @param serviceLevel MasterDomain service level.
   */
  public void setServiceLevel(int serviceLevel) {
    this.serviceLevel = serviceLevel;
  }

  @Override
  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    JsonUtils.setSafeString(jsonObject, "name", name);
    JsonUtils.setSafeInt(jsonObject, "serviceLevel", serviceLevel);
    JsonUtils.setSafeString(jsonObject, "parent", parent);
    return jsonObject.toString();
  }

  @Override
  public void fromJson(String json) {
    JSONObject jsonObject = new JSONObject(json);
    this.name = JsonUtils.getSafeString(jsonObject, "name");
    this.serviceLevel = JsonUtils.getSafeInt(jsonObject, "serviceLevel");
    this.parent = JsonUtils.getSafeString(jsonObject, "parent");
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.masterdomains";
  }

  @Override
  public <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
    return (object == null) ? null
            : (T) new MasterDomain(
                    JsonUtils.getSafeString(object, "name"),
                    JsonUtils.getSafeInt(object, "serviceLevel"),
                    JsonUtils.getSafeBoolean(object, "inScope"),
                    JsonUtils.getSafeBoolean(object, "reviewed"),
                    toDate(JsonUtils.getSafeString(object, "last_scan_date")),
                    JsonUtils.getSafeString(object, "parent"));
  }

  @Override
  public boolean upsert() throws DatalakeStorageException {
    boolean result = (find() != null);
    if (result) {
      result &= this.upsertField("name", name);
      result &= this.upsertField("serviceLevel", serviceLevel);
      result &= this.upsertField("inScope", inScope);
      result &= this.upsertField("reviewed", reviewed);
      result &= this.upsertField("last_scan_date", fromDate(getLastScanDate()));
    }
    return result;
  }

}
