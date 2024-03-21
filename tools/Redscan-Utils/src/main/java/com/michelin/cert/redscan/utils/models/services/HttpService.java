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
 * Http Service model.
 *
 * @author Maxime ESCOURBIAC
 */
public class HttpService extends DatalakeStorageItem implements Sendable {

  private String domain;
  private String ip;
  private String port;
  private boolean ssl;

  /**
   * HTTP Service Constructor.
   */
  public HttpService() {
    index = "http_services";
    ssl = false;
  }

  /**
   * HTTP Service Constructor.
   *
   * @param domain Service domain.
   * @param ip Service ip.
   * @param port Service port.
   * @param isSsl HTTP/HTTPS
   */
  public HttpService(String domain, String ip, String port, boolean isSsl) {
    this();
    this.domain = domain;
    this.ip = ip;
    this.port = port;
    this.ssl = isSsl;
  }

  /**
   * HTTP Service Constructor.
   *
   * @param domain Service domain.
   * @param ip Service ip.
   * @param port Service port.
   * @param parent Parent Service.
   * @param isSsl HTTP/HTTPS.
   */
  public HttpService(String domain, String ip, String port, String parent, boolean isSsl) {
    this(domain, ip, port, isSsl);
    this.parent = parent;
  }

  /**
   * Convert the HTTP Service to url.
   *
   * @return The String representation of the HTTP service.
   */
  public String toUrl() {
    return String.format("%s%s:%s", (ssl) ? "https://" : "http://", getDomain(), getPort());
  }

  @Override
  public void fromJson(String json) {
    JSONObject jsonObject = new JSONObject(json);
    this.domain = JsonUtils.getSafeString(jsonObject, "domain");
    this.ip = JsonUtils.getSafeString(jsonObject, "ip");
    this.port = JsonUtils.getSafeString(jsonObject, "port");
    this.ssl = JsonUtils.getSafeBoolean(jsonObject, "ssl");
  }

  @Override
  public String toJson() {
    JSONObject jsonObject = new JSONObject();
    JsonUtils.setSafeString(jsonObject, "domain", this.domain);
    JsonUtils.setSafeString(jsonObject, "ip", this.ip);
    JsonUtils.setSafeString(jsonObject, "port", this.port);
    JsonUtils.setSafeBoolean(jsonObject, "ssl", this.ssl);
    return jsonObject.toString();
  }

  @Override
  public String getId() {
    return String.format("%s_%s_%s", getDomain(), getPort(), (ssl) ? "https" : "http");
  }

  @Override
  public String getFanoutExchangeName() {
    return "com.michelin.cert.fanout.httpservices";
  }

  @Override
  public <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
    return (object == null) ? null
            : (T) new HttpService(
                    JsonUtils.getSafeString(object, "domain"),
                    JsonUtils.getSafeString(object, "ip"),
                    JsonUtils.getSafeString(object, "port"),
                    JsonUtils.getSafeString(object, "parent"),
                    JsonUtils.getSafeBoolean(object, "ssl"));
  }

  @Override
  public boolean upsert() throws DatalakeStorageException {
    boolean result = (find() != null);
    if (result) {
      result &= this.upsertField("domain", domain);
      result &= this.upsertField("ip", ip);
      result &= this.upsertField("port", port);
      result &= this.upsertField("ssl", ssl);
    }
    return result;
  }

  /**
   * HTTP Service domain.
   *
   * @return HTTP Service domain.
   */
  public String getDomain() {
    return domain;
  }

  /**
   * HTTP Service domain.
   *
   * @param domain HTTP Service domain.
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * HTTP Service ip.
   *
   * @return HTTP Service ip.
   */
  public String getIp() {
    return ip;
  }

  /**
   * HTTP Service ip.
   *
   * @param ip HTTP Service ip.
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * HTTP Service port.
   *
   * @return HTTP Service port.
   */
  public String getPort() {
    return port;
  }

  /**
   * HTTP Service port.
   *
   * @param port HTTP Service port.
   */
  public void setPort(String port) {
    this.port = port;
  }

  /**
   * HTTP Service protocol (http, https).
   *
   * @return HTTP Service protocol (http, https).
   */
  public boolean isSsl() {
    return ssl;
  }

  /**
   * HTTP Service protocol (http, https).
   *
   * @param isSsl HTTP Service protocol (http, https)
   */
  public void setSsl(boolean isSsl) {
    this.ssl = isSsl;
  }
}
