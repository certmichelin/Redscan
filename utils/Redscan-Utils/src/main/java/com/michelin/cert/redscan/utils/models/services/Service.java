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

package com.michelin.cert.redscan.utils.models.services;

import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.json.JsonUtils;
import com.michelin.cert.redscan.utils.models.Sendable;

import kong.unirest.json.JSONObject;

/**
 * Service model.
 *
 * @author Maxime ESCOURBIAC.
 */
public class Service extends DatalakeStorageItem implements Sendable {

  private String domain;
  private String ip;
  private String port;
  private String name;
  private String tunnel;
  private String banner;
  private String protocol;
  private String state;
  private String product;
  private String version;

  /**
   * Default constructor.
   */
  public Service() {
    index = "services";
  }

  /**
   * Service Constructor.
   *
   * @param domain Service domain.
   * @param ip Service ip.
   * @param port Service port.
   */
  public Service(String domain, String ip, String port) {
    this();
    this.domain = domain;
    this.ip = ip;
    this.port = port;
  }

  /**
   * Service Constructor.
   *
   * @param domain Service domain.
   * @param ip Service ip.
   * @param port Service port.
   * @param protocol Service protocol.
   */
  public Service(String domain, String ip, String port, String protocol) {
    this();
    this.domain = domain;
    this.ip = ip;
    this.port = port;
    this.protocol = protocol;
  }

  /**
   * Service Constructor.
   *
   * @param domain Service domain.
   * @param ip Service ip.
   * @param port Service port.
   * @param protocol Service protocol.
   * @param parent Service parent.
   */
  public Service(String domain, String ip, String port, String protocol, String parent) {
    this(domain, ip, port, protocol);
    this.parent = parent;
  }

  /**
   * Service Constructor.
   *
   * @param domain Service domain.
   * @param ip Service ip.
   * @param port Service port.
   * @param name Service name.
   * @param tunnel Service tunnel.
   * @param protocol Service protocol.
   * @param state Service state.
   * @param product Service product.
   * @param version Service version.
   * @param parent Service parent.
   */
  public Service(String domain, String ip, String port, String name, String tunnel, String protocol, String state, String product, String version, String parent) {
    this(domain, ip, port, protocol, parent);
    this.name = name;
    this.tunnel = tunnel;
    this.state = state;
    this.product = product;
    this.version = version;
  }

  @Override
  public String getId() {
    return String.format("%s_%s_%s", domain, port, protocol);
  }

  @Override
  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    JsonUtils.setSafeString(jsonObject, "domain", domain);
    JsonUtils.setSafeString(jsonObject, "parent", parent);
    JsonUtils.setSafeString(jsonObject, "ip", ip);
    JsonUtils.setSafeString(jsonObject, "port", port);
    JsonUtils.setSafeString(jsonObject, "name", name);
    JsonUtils.setSafeString(jsonObject, "tunnel", tunnel);
    JsonUtils.setSafeString(jsonObject, "banner", banner);
    JsonUtils.setSafeString(jsonObject, "protocol", protocol);
    JsonUtils.setSafeString(jsonObject, "state", state);
    JsonUtils.setSafeString(jsonObject, "product", product);
    JsonUtils.setSafeString(jsonObject, "version", version);
    return jsonObject.toString();
  }

  @Override
  public void fromJson(String json) {
    JSONObject jsonObject = new JSONObject(json);
    this.domain = JsonUtils.getSafeString(jsonObject, "domain");
    this.parent = JsonUtils.getSafeString(jsonObject, "parent");
    this.ip = JsonUtils.getSafeString(jsonObject, "ip");
    this.port = JsonUtils.getSafeString(jsonObject, "port");
    this.name = JsonUtils.getSafeString(jsonObject, "name");
    this.tunnel = JsonUtils.getSafeString(jsonObject, "tunnel");
    this.banner = JsonUtils.getSafeString(jsonObject, "banner");
    this.protocol = JsonUtils.getSafeString(jsonObject, "protocol");
    this.state = JsonUtils.getSafeString(jsonObject, "state");
    this.product = JsonUtils.getSafeString(jsonObject, "product");
    this.version = JsonUtils.getSafeString(jsonObject, "version");
  }

  @Override
  public <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
    return (object == null) ? null
            : (T) new Service(
                    JsonUtils.getSafeString(object, "domain"),
                    JsonUtils.getSafeString(object, "ip"),
                    JsonUtils.getSafeString(object, "port"),
                    JsonUtils.getSafeString(object, "name"),
                    JsonUtils.getSafeString(object, "tunnel"),
                    JsonUtils.getSafeString(object, "protocol"),
                    JsonUtils.getSafeString(object, "state"),
                    JsonUtils.getSafeString(object, "product"),
                    JsonUtils.getSafeString(object, "version"),
                    JsonUtils.getSafeString(object, "parent"));
  }

  @Override
  public boolean upsert() throws DatalakeStorageException {
    boolean result = (find() != null);
    if (result) {
      result &= this.upsertField("domain", domain);
      result &= this.upsertField("ip", ip);
      result &= this.upsertField("port", port);
      result &= this.upsertField("name", name);
      result &= this.upsertField("tunnel", tunnel);
      result &= this.upsertField("protocol", protocol);
      result &= this.upsertField("state", state);
      result &= this.upsertField("product", product);
      result &= this.upsertField("version", version);
    }
    return result;
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.services";
  }

  /**
   * Service domain.
   *
   * @return Service domain.
   */
  public String getDomain() {
    return domain;
  }

  /**
   * Service domain.
   *
   * @param domain Service domain.
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Service ip.
   *
   * @return Service ip.
   */
  public String getIp() {
    return ip;
  }

  /**
   * Service ip.
   *
   * @param ip Service ip.
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Service port.
   *
   * @return Service port.
   */
  public String getPort() {
    return port;
  }

  /**
   * Service port.
   *
   * @param port Service port.
   */
  public void setPort(String port) {
    this.port = port;
  }

  /**
   * Service name.
   *
   * @return Service name.
   */
  public String getName() {
    return name;
  }

  /**
   * Service name.
   *
   * @param name Service name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Service tunnel.
   *
   * @return Service tunnel.
   */
  public String getTunnel() {
    return tunnel;
  }

  /**
   * Service tunnel.
   *
   * @param tunnel Service tunnel.
   */
  public void setTunnel(String tunnel) {
    this.tunnel = tunnel;
  }

  /**
   * Service banner.
   *
   * @return Service banner.
   */
  public String getBanner() {
    return banner;
  }

  /**
   * Service banner.
   *
   * @param banner Service banner.
   */
  public void setBanner(String banner) {
    this.banner = banner;
  }

  /**
   * Service protocol.
   *
   * @return Service protocol.
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * Service protocol.
   *
   * @param protocol Service protocol.
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Service state.
   *
   * @return Service state.
   */
  public String getState() {
    return state;
  }

  /**
   * Service state.
   *
   * @param state Service state.
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Service product.
   *
   * @return Service product.
   */
  public String getProduct() {
    return product;
  }

  /**
   * Service product.
   *
   * @param product Service product.
   */
  public void setProduct(String product) {
    this.product = product;
  }

  /**
   * Service version.
   *
   * @return Service version.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Service version.
   *
   * @param version Service version.
   */
  public void setVersion(String version) {
    this.version = version;
  }
}
