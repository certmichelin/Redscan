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

import com.fasterxml.jackson.annotation.JsonFormat;

import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.json.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import kong.unirest.json.JSONObject;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.logging.log4j.LogManager;

/**
 * IP range model.
 *
 * @author Maxime ESCOURBIAC.
 */
public class IpRange extends DatalakeStorageItem implements Sendable, Serializable {

  private String cidr;
  private int serviceLevel;
  private String description;

  @JsonFormat(pattern = "yyyy-MM-dd kk:mm")
  private Date lastScanDate;

  /**
   * Default constructor.
   */
  public IpRange() {
    index = "iprange";
    parent = null;
  }

  /**
   * IpRange constructor.
   *
   * @param cidr Classless Inter-Domain Routing.
   */
  public IpRange(String cidr) {
    this();
    this.cidr = cidr;
  }

  /**
   * IpRange constructor.
   *
   * @param cidr Classless Inter-Domain Routing.
   * @param description Ip range description.
   * @param serviceLevel Ip range service level.
   * @param lastScanDate Last scan date.
   */
  public IpRange(String cidr, String description, int serviceLevel, Date lastScanDate) {
    this(cidr);
    this.description = description;
    this.serviceLevel = serviceLevel;
    this.lastScanDate = lastScanDate;
  }

  /**
   * Check if CIDR is valid.
   *
   * @return True if CIDR is valid.
   */
  public boolean isValid() {
    boolean result = false;
    try {
      new SubnetUtils(cidr);
      result = true;
    } catch (IllegalArgumentException ex) {
      LogManager.getLogger(IpRange.class).info(String.format("not valid (%s) : %s", cidr, ex.getMessage()));
    }
    return result;
  }

  /**
   * Get ip addresses from CIDR, including network and broadcast addresses.
   *
   * @return Ip addresses from CIDR, including network and broadcast addresses.
   */
  public List<String> toIpList() {
    List<String> ips = new ArrayList<>();

    try {
      SubnetUtils subnetUtils = new SubnetUtils(cidr);
      Collections.addAll(ips, subnetUtils.getInfo().getAllAddresses());

      String networkAddress = subnetUtils.getInfo().getNetworkAddress();
      if (!ips.contains(networkAddress)) {
        ips.add(networkAddress);
      }

      String broadcastAddress = subnetUtils.getInfo().getBroadcastAddress();
      if (!ips.contains(broadcastAddress)) {
        ips.add(broadcastAddress);
      }
    } catch (IllegalArgumentException ex) {
      LogManager.getLogger(IpRange.class).error(String.format("IllegalArgumentException (%s) : %s", cidr, ex.getMessage()));
      ips = null;
    }

    return ips;
  }

  /**
   * Classless Inter-Domain Routing.
   *
   * @return Classless Inter-Domain Routing.
   */
  public String getCidr() {
    return cidr;
  }

  /**
   * Classless Inter-Domain Routing.
   *
   * @param cidr Classless Inter-Domain Routing.
   */
  public void setCidr(String cidr) {
    this.cidr = cidr;
  }

  /**
   * Ip range description.
   *
   * @return Ip range description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Ip range description.
   *
   * @param description Ip range description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Last scan date.
   *
   * @return Last scan date.
   */
  public Date getLastScanDate() {
    return lastScanDate;
  }

  /**
   * Last scan date.
   *
   * @param lastScanDate Last scan date.
   */
  public void setLastScanDate(Date lastScanDate) {
    this.lastScanDate = lastScanDate;
  }

  /**
   * Ip range service level.
   *
   * @return Ip range service level.
   */
  public int getServiceLevel() {
    return serviceLevel;
  }

  /**
   * Ip range service level.
   *
   * @param serviceLevel Ip range service level.
   */
  public void setServiceLevel(int serviceLevel) {
    this.serviceLevel = serviceLevel;
  }

  @Override
  protected <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
    return (object == null) ? null
            : (T) new IpRange(JsonUtils.getSafeString(object, "cidr"),
                    JsonUtils.getSafeString(object, "description"),
                    JsonUtils.getSafeInt(object, "serviceLevel"),
                    toDate(JsonUtils.getSafeString(object, "last_scan_date")));
  }

  @Override
  public boolean upsert() throws DatalakeStorageException {
    boolean result = (find() != null);
    if (result) {
      result &= this.upsertField("cidr", cidr);
      result &= this.upsertField("description", description);
      result &= this.upsertField("serviceLevel", serviceLevel);
      result &= this.upsertField("last_scan_date", fromDate(lastScanDate));
    }
    return result;
  }

  @Override
  public String getId() {
    return cidr.replace("/", "_");
  }

  @Override
  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    JsonUtils.setSafeString(jsonObject, "cidr", cidr);
    JsonUtils.setSafeInt(jsonObject, "serviceLevel", serviceLevel);
    return jsonObject.toString();
  }

  @Override
  public void fromJson(String json) {
    JSONObject jsonObject = new JSONObject(json);
    this.cidr = JsonUtils.getSafeString(jsonObject, "cidr");
    this.serviceLevel = JsonUtils.getSafeInt(jsonObject, "serviceLevel");
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.iprange";
  }

}
