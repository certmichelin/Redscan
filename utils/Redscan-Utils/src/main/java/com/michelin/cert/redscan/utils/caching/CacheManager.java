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

package com.michelin.cert.redscan.utils.caching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

/**
 * Cache management.
 *
 * @author Maxime ESCOURBIAC
 */
public class CacheManager {

  protected String applicationName;
  protected String cacheManagerUrl;

  private static final int HTTP_OK = 200;

  /**
   * CacheManager default constructor.
   */
  public CacheManager() {
  }

  /**
   * CacheManager constructor.
   *
   * @param applicationName Application Name to avoid conflict in cache.
   * @param cacheManagerUrl Cache Manager url (ex: http://127.0.0.1:8080 )
   */
  public CacheManager(String applicationName, String cacheManagerUrl) {
    this.applicationName = applicationName;
    this.cacheManagerUrl = cacheManagerUrl;
  }

  /**
   * Generate a unique key for the cache. WARNING : MUST CONTAINS
   *
   * @param keyValues Key values.
   * @return Unique key.
   */
  public String buildKey(String... keyValues) {
    StringBuilder key = new StringBuilder(applicationName);
    for (String keyValue : keyValues) {
      key.append("_").append(keyValue);
    }
    return key.toString();
  }

  /**
   * Get cache entry.
   *
   * @param key Cache key to retrieve.
   * @param validity validity in hours.
   * @return The retrieve cache.
   */
  public Map<String, String> getCache(String key, int validity) {
    Map<String, String> result = null;
    LogManager.getLogger(CacheManager.class).info(String.format("Get Cache for : %s with %d hour(s) validity", key, validity));
    HttpResponse<JsonNode> jsonResponse = Unirest.get(String.format("%s/cache/%s/%d", cacheManagerUrl, key, validity)).asJson();
    if (jsonResponse.getStatus() == HTTP_OK) {
      LogManager.getLogger(CacheManager.class).info(String.format("Cache hit for : %s", key));
      result = new HashMap<>();
      JSONObject cacheMap = jsonResponse.getBody().getObject().getJSONObject("cacheMap");
      Iterator<String> cacheKeys = cacheMap.keys();
      while (cacheKeys.hasNext()) {
        String cacheKey = cacheKeys.next();
        result.put(cacheKey, cacheMap.getString(cacheKey));
        LogManager.getLogger(CacheManager.class).debug(String.format("Cache retrieved for : %s, [%s] = %s", key, cacheKey, cacheMap.getString(cacheKey)));
      }
    } else {
      LogManager.getLogger(CacheManager.class).info(String.format("Cache not found for : %s", key));
    }
    return result;
  }

  /**
   * Set cache entry.
   *
   * @param key Cache key to retrieve.
   * @param values Values to cache.
   * @return True if cached.
   */
  public boolean setCache(String key, Map<String, String> values) {
    LogManager.getLogger(CacheManager.class).info(String.format("Set Cache for : %s", key));
    JSONObject cacheEntry = new JSONObject();
    cacheEntry.put("cacheKey", key);
    JSONObject cacheValues = new JSONObject();
    values.entrySet().forEach(me -> {
      cacheValues.put((String) me.getKey(), (String) me.getValue());
      LogManager.getLogger(CacheManager.class).debug(String.format("Cache value for : %s, [%s] = %s", key, me.getKey(), me.getValue()));
    });
    cacheEntry.put("cacheMap", cacheValues);
    HttpResponse<JsonNode> response = Unirest.post(String.format("%s/cache", cacheManagerUrl)).header("Content-Type", "application/json").body(cacheEntry).asJson();
    LogManager.getLogger(CacheManager.class).info(String.format("Set Cache for : %s, response code : %d", key, response.getStatus()));
    return response.getStatus() == HTTP_OK;
  }

}
