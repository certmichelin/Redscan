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

package com.michelin.cert.redscan.utils.json;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

/**
 * Json Utils.
 *
 * @author Maxime ESCOURBIAC
 */
public class JsonUtils {

  /**
   * Safe method to extract string from Json Object.
   *
   * @param json Json object.
   * @param key Key to extract.
   * @return Get the string associated with a key.
   */
  public static String getSafeString(JSONObject json, String key) {
    String safeString = "";
    if (json != null && json.has(key) && !json.isNull(key)) {
      safeString = json.getString(key);
    }
    return safeString;
  }

  /**
   * Safe method to inject string to Json Object.
   *
   * @param json Json object.
   * @param key Key to inject.
   * @param value Value to inject.
   */
  public static void setSafeString(JSONObject json, String key, String value) {
    if (json != null && key != null && value != null) {
      json.put(key, value);
    }
  }

  /**
   * Safe method to extract int from Json Object.
   *
   * @param json Json object.
   * @param key Key to extract.
   * @return Get the int associated with a key.
   */
  public static int getSafeInt(JSONObject json, String key) {
    int safeInt = -1;
    if (json != null && json.has(key) && !json.isNull(key)) {
      safeInt = json.getInt(key);
    }
    return safeInt;
  }

  /**
   * Safe method to inject int to Json Object.
   *
   * @param json Json object.
   * @param key Key to inject.
   * @param value Value to inject.
   */
  public static void setSafeInt(JSONObject json, String key, int value) {
    if (json != null && key != null) {
      json.put(key, value);
    }
  }

  /**
   * Safe method to extract boolean from Json Object.
   *
   * @param json Json object.
   * @param key Key to extract.
   * @return Get the boolean associated with a key.
   */
  public static boolean getSafeBoolean(JSONObject json, String key) {
    boolean safeBoolean = false;
    if (json != null && json.has(key) && !json.isNull(key)) {
      safeBoolean = json.getBoolean(key);
    }
    return safeBoolean;
  }

  /**
   * Safe method to inject boolean to Json Object.
   *
   * @param json Json object.
   * @param key Key to inject.
   * @param value Value to inject.
   */
  public static void setSafeBoolean(JSONObject json, String key, Boolean value) {
    if (json != null && key != null) {
      json.put(key, value);
    }
  }

  /**
   * Safe method to inject string array to Json Object.
   *
   * @param json Json object.
   * @param key Key to extract.
   * @return Get the string array associated with a key.
   */
  public static String[] getSafeStringArray(JSONObject json, String key) {
    String[] safeArray = {};
    if (json != null && json.has(key) && !json.isNull(key)) {
      JSONArray jsonArray = json.getJSONArray(key);
      safeArray = new String[jsonArray.length()];
      for (int i = 0; i < jsonArray.length(); i++) {
        safeArray[i] = jsonArray.getString(i);
      }
    }
    return safeArray;
  }

  /**
   * Safe method to inject string array to Json Object.
   *
   * @param json Json object.
   * @param key Key to inject.
   * @param values Values to inject.
   */
  public static void setSafeStringArray(JSONObject json, String key, String[] values) {
    if (json != null && key != null) {
      JSONArray jsonArray = new JSONArray();
      if (values != null) {
        for (String value : values) {
          jsonArray.put(value);
        }
      }
      json.put(key, jsonArray);
    }
  }
}
