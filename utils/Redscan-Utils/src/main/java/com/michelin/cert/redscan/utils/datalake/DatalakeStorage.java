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

package com.michelin.cert.redscan.utils.datalake;

import java.text.SimpleDateFormat;
import java.util.Date;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

/**
 * Datalake storage.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */
public class DatalakeStorage {

  // Number of record to be return by elasticsearch
  private static final int NB_RECORD = 1000;

  // Keep alive of pit for returning more than 1000 records
  private static final String PIT_KEEP_ALIVE = "1m";

  //Timestamp format.
  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  //Elastic Search url
  private static String elasticSearchUrl;
  
  //Elastic Search user
  private static String elasticSearchUser;
  
  //Elastic Search password
  private static String elasticSearchPassword;

  /**
   * DatalakeStorage default constructor.
   */
  private DatalakeStorage() {
  }

  /**
   * Init the datalake storage.
   *
   * @param elasticSearchUrl Elastic Search url (ex: http://10.124.2.25:9200 )
   * @param elasticSearchUser Elastic Search user.
   * @param elasticSearchPassword Elastic Search password.
   */
  public static void init(String elasticSearchUrl, String elasticSearchUser, String elasticSearchPassword) {
    Unirest.config().verifySsl(false);
    DatalakeStorage.elasticSearchUrl = elasticSearchUrl;
    DatalakeStorage.elasticSearchUser = elasticSearchUser;
    DatalakeStorage.elasticSearchPassword = elasticSearchPassword;
  }

  /**
   * Search objects with sorting.
   *
   * @param item Datalake storage item.
   * @param query Elastic search query.
   * @param sort Elastic sort query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObjects(DatalakeStorageItem item, JSONObject query, JSONObject sort) throws DatalakeStorageException {
    return getObjects(item.getIndex(), query, sort);
  }

  /**
   * Search objects.
   *
   * @param item Datalake storage item.
   * @param query Elastic search query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObjects(DatalakeStorageItem item, JSONObject query) throws DatalakeStorageException {
    return getObjects(item.getIndex(), query, null);
  }

  /**
   * Search objects.
   *
   * @param index ElasticSearch index.
   * @param query Elastic search query.
   * @param sort Elastic sort query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObjects(String index, JSONObject query, JSONObject sort) throws DatalakeStorageException {
    JSONObject results = null;

    try {
      //Initialize query : If null define *
      query = (query != null) ? query : new JSONObject("{\"query_string\" : {\"query\":\"*\"}}");
      // Initialize sorting : If null define asc sorting by _id
      sort = (sort != null) ? sort : new JSONObject("{\"_id\" :\"asc\"}");

      // To "freeze" the index elasticsearch use a PIT(Point In Time)
      // https://www.elastic.co/guide/en/elasticsearch/reference/7.x/point-in-time-api.html
      // Request PIT on index
      HttpResponse<String> res = Unirest.post(String.format("%s/%s/_pit?keep_alive=%s", elasticSearchUrl, index, PIT_KEEP_ALIVE)).basicAuth(elasticSearchUser, elasticSearchPassword).asString();
      if (res.getStatus() == DatalakeStorageResponseCode.HTTP_OK) {
        JSONArray objects = new JSONArray();
        JSONArray searchAfterArray = null;
        JSONObject hitsObject;

        //Retrieve Pit resul and insert keep_alive.
        JSONObject pit = new JSONObject(res.getBody());
        pit.put("keep_alive", PIT_KEEP_ALIVE);

        //Configure the query.
        JSONObject jsonRequestObject = new JSONObject();
        jsonRequestObject.put("query", query);
        jsonRequestObject.put("size", NB_RECORD);
        jsonRequestObject.put("sort", sort);
        jsonRequestObject.put("pit", pit);

        boolean searchEnded = false;
        while (!searchEnded) {

          // Inject search after
          if (searchAfterArray != null) {
            jsonRequestObject.put("search_after", searchAfterArray);
          }

          // Make search request
          res = Unirest.post(String.format("%s/_search", elasticSearchUrl)).header("Content-Type", "application/json").body(jsonRequestObject).basicAuth(elasticSearchUser, elasticSearchPassword).asString();
          if (res.getStatus() == DatalakeStorageResponseCode.HTTP_OK) {

            // Get result and put it in result array
            JSONObject searchResult = new JSONObject(res.getBody());
            hitsObject = searchResult.getJSONObject("hits");

            // No elements return by search
            if (hitsObject != null) {
              JSONArray currentHits = hitsObject.getJSONArray("hits");
              if (currentHits != null && currentHits.length() > 0) {
                int nbHits = 0;

                while (nbHits < currentHits.length()) {
                  objects.put(currentHits.getJSONObject(nbHits++));
                }

                if (nbHits != 0) {
                  searchAfterArray = currentHits.getJSONObject(nbHits - 1).getJSONArray("sort");

                  //Get the PIT return and reinject it
                  pit.put("id", searchResult.getString("pit_id"));
                }
              } else {
                searchEnded = true;
              }
            } else {
              searchEnded = true;
            }
          } else {
            LogManager.getLogger(DatalakeStorageItem.class).error(String.format("GetObjects (%s) status : %d body : %s", index, res.getStatus(), res.getBody()));
            searchEnded = true;
          }
        }

        //Delete PIT
        Unirest.delete(String.format("%s/_pit", elasticSearchUrl)).header("Content-Type", "application/json").body(pit).basicAuth(elasticSearchUser, elasticSearchPassword).asString();

        //Update results.
        if (objects.length() > 0) {
          //Simulate total returned from ES.
          JSONObject total = new JSONObject();
          total.put("relation", "eq");
          total.put("value", objects.length());
          results = new JSONObject();
          results.put("total", total);
          results.put("hits", objects);
        }
      } else {
        LogManager.getLogger(DatalakeStorageItem.class).error(String.format("GetObjects (%s) create PIT failed : %d body : %s", index, res.getStatus(), res.getBody()));
      }
    } catch (Exception ex) {
      LogManager.getLogger(DatalakeStorageItem.class).error(String.format("GetObjects (Index : %s) (Query: %s) : %s", index, query, ex.getMessage()));
      throw new DatalakeStorageException(String.format("GetObjects (Index : %s) (Query: %s) : %s", index, query, ex.getMessage()), ex);
    }
    return results;
  }

  /**
   * Search objects with sorting and pagination.
   *
   * @param item Datalake storage item.
   * @param query Elastic search query.
   * @param page Page number.
   * @param size Number of items in each page.
   * @param sort Elastic sort query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObjects(DatalakeStorageItem item, JSONObject query, String page, String size, JSONObject sort) throws DatalakeStorageException {
    return getObjects(item.getIndex(), query, page, size, sort);
  }

  /**
   * Search objects with pagination.
   *
   * @param item Datalake storage item.
   * @param query Elastic search query.
   * @param page Page number.
   * @param size Number of items in each page.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObjects(DatalakeStorageItem item, JSONObject query, String page, String size) throws DatalakeStorageException {
    return getObjects(item.getIndex(), query, page, size, null);
  }

  /**
   * Search objects with pagination.
   *
   * @param index ElasticSearch index.
   * @param query Elastic search query.
   * @param page Page number.
   * @param size Number of items in each page.
   * @param sort Elastic sort query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObjects(String index, JSONObject query, String page, String size, JSONObject sort) throws DatalakeStorageException {
    JSONObject results = null;

    try {
      //Initialize query : If null define *
      query = (query != null) ? query : new JSONObject("{\"query_string\" : {\"query\":\"*\"}}");
      // Initialize sorting : If null define asc sorting by _id
      sort = (sort != null) ? sort : new JSONObject("{\"_id\" :\"asc\"}");

      JSONArray objects = new JSONArray();
      JSONObject hitsObject;

      //Configure the query.
      JSONObject jsonRequestObject = new JSONObject();
      jsonRequestObject.put("query", query);
      jsonRequestObject.put("size", size == null ? NB_RECORD : Integer.parseInt(size));
      jsonRequestObject.put("from", page == null ? 0 : (Integer.parseInt(page) - 1) * Integer.parseInt(size));
      jsonRequestObject.put("sort", sort);

      HttpResponse<String> res = Unirest.post(String.format("%s/%s/_search", elasticSearchUrl, index)).header("Content-Type", "application/json").body(jsonRequestObject).basicAuth(elasticSearchUser, elasticSearchPassword).asString();

      if (res.getStatus() == DatalakeStorageResponseCode.HTTP_OK) {
        // Get result and put it in result array
        JSONObject searchResult = new JSONObject(res.getBody());
        hitsObject = searchResult.getJSONObject("hits");

        // No elements return by search
        if (hitsObject != null) {
          JSONArray currentHits = hitsObject.getJSONArray("hits");
          if (currentHits != null && currentHits.length() > 0) {
            int nbHits = 0;

            while (nbHits < currentHits.length()) {
              objects.put(currentHits.getJSONObject(nbHits++));
            }
          }
        }
      } else {
        LogManager.getLogger(DatalakeStorageItem.class).error(String.format("GetObjects (%s) status : %d body : %s", index, res.getStatus(), res.getBody()));
      }

      //Update results.
      if (objects.length() > 0) {
        //Simulate total returned from ES.
        JSONObject total = new JSONObject();
        total.put("relation", "eq");
        total.put("value", objects.length());
        results = new JSONObject();
        results.put("total", total);
        results.put("hits", objects);
      }

    } catch (Exception ex) {
      LogManager.getLogger(DatalakeStorageItem.class).error(String.format("GetObjects (Index : %s) (Query: %s) : %s", index, query, ex.getMessage()));
      throw new DatalakeStorageException(String.format("GetObjects (Index : %s) (Query: %s) : %s", index, query, ex.getMessage()), ex);
    }
    return results;
  }

  /**
   * Retrieve an object from Elastic search.
   *
   * @param item Item model to retrieve.
   * @return The object corresponding to the model.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObject(DatalakeStorageItem item) throws DatalakeStorageException {
    return getObject(item, item.getId());
  }

  /**
   * Retrieve an object from Elastic search.
   *
   * @param item Item model to retrieve.
   * @param id Item id.
   * @return The object corresponding to the model.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObject(DatalakeStorageItem item, String id) throws DatalakeStorageException {
    return getObject(item.getIndex(), id);
  }

  /**
   * Retrieve an object from Elastic search.
   *
   * @param index ElasticSearch index.
   * @param id Item id.
   * @return The object corresponding to the model.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static JSONObject getObject(String index, String id) throws DatalakeStorageException {
    JSONObject result = null;
    try {
      HttpResponse<String> res = Unirest.get(String.format("%s/%s/_doc/%s", elasticSearchUrl, index, id)).basicAuth(elasticSearchUser, elasticSearchPassword).asString();
      if (res.getStatus() != DatalakeStorageResponseCode.HTTP_OK && res.getStatus() != DatalakeStorageResponseCode.HTTP_NOT_FOUND) {
        LogManager.getLogger(DatalakeStorageItem.class).info(String.format("GetObject (%s) from index %s status : %d body : %s", id, index, res.getStatus(), res.getBody()));
      }
      result = (res.getStatus() == DatalakeStorageResponseCode.HTTP_OK) ? (new JSONObject(res.getBody())).getJSONObject("_source") : null;
    } catch (Exception ex) {
      LogManager.getLogger(DatalakeStorageItem.class).error(String.format("GetObject (%s) from index %s : %s", id, index, ex.getMessage()));
      throw new DatalakeStorageException(String.format("GetObject (%s) from index %s : %s", id, index, ex.getMessage()), ex);
    }
    return result;
  }

  /**
   * Insert a new object in ElasticSearch.
   *
   * @param item Item model to create.
   * @return True if the creation is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean createObject(DatalakeStorageItem item) throws DatalakeStorageException {
    boolean result = false;
    if (item.findContent(null) == null) {
      JSONObject jsonId = new JSONObject();
      jsonId.put("id", item.getId());
      if (item.getParent() != null) {
        jsonId.put("parent", item.getParent());
      }
      jsonId.put("@timestamp", generateDate());
      result = createObject(item.getIndex(), item.getId(), jsonId);
    }
    return result;
  }

  /**
   * Insert a new object in ElasticSearch.
   *
   * @param index ElasticSearch index.
   * @param id Item id.
   * @return True if the creation is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean createObject(String index, String id) throws DatalakeStorageException {
    JSONObject jsonId = new JSONObject();
    jsonId.put("id", id);
    jsonId.put("@timestamp", generateDate());
    return createObject(index, id, jsonId);
  }

  /**
   * Insert a new object in ElasticSearch.
   *
   * @param index ElasticSearch index.
   * @param id Item id.
   * @param jsonObject Initial JSON object
   * @return True if the creation is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean createObject(String index, String id, JSONObject jsonObject) throws DatalakeStorageException {
    boolean result = false;
    try {
      HttpResponse res = Unirest.post(String.format("%s/%s/_create/%s", elasticSearchUrl, index, id)).header("Content-Type", "application/json").body(jsonObject).basicAuth(elasticSearchUser, elasticSearchPassword).asEmpty();
      if (res.getStatus() != DatalakeStorageResponseCode.HTTP_CREATED) {
        LogManager.getLogger(DatalakeStorageItem.class).info(String.format("CreateObject (%s) from index %s status : %d body : %s", id, index, res.getStatus(), res.getBody()));
      }
      result = (res.getStatus() == DatalakeStorageResponseCode.HTTP_CREATED);
    } catch (Exception ex) {
      LogManager.getLogger(DatalakeStorageItem.class).error(String.format("CreateObject (%s) from index %s : %s", id, index, ex.getMessage()));
      throw new DatalakeStorageException(String.format("CreateObject (%s) from index %s : %s", id, index, ex.getMessage()), ex);
    }
    return result;
  }

  /**
   * Delete an object from ElasticSearch.
   *
   * @param item Item model to delete.
   * @return True if the deletion is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean deleteObject(DatalakeStorageItem item) throws DatalakeStorageException {
    return deleteObject(item.getIndex(), item.getId());
  }

  /**
   * Delete an object from ElasticSearch.
   *
   * @param index ElasticSearch index.
   * @param id Item id.
   * @return True if the deletion is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean deleteObject(String index, String id) throws DatalakeStorageException {
    boolean result = false;
    try {
      HttpResponse res = Unirest.delete(String.format("%s/%s/_doc/%s", elasticSearchUrl, index, id)).basicAuth(elasticSearchUser, elasticSearchPassword).asEmpty();
      if (res.getStatus() != DatalakeStorageResponseCode.HTTP_OK) {
        LogManager.getLogger(DatalakeStorageItem.class).info(String.format("DeleteObject (%s) from index %s status : %d body : %s", id, index, res.getStatus(), res.getBody()));
      }
      result = (res.getStatus() == DatalakeStorageResponseCode.HTTP_OK);
    } catch (Exception ex) {
      LogManager.getLogger(DatalakeStorageItem.class).error(String.format("DeleteObject (%s) from index %s : %s", id, index, ex.getMessage()));
      throw new DatalakeStorageException(String.format("DeleteObject (%s) from index %s : %s", id, index, ex.getMessage()), ex);
    }
    return result;
  }

  /**
   * Insert or Update a specific field for an object.
   *
   * @param item Item model to upsert.
   * @param key Key to upsert.
   * @param value Value to upsert.
   * @return True if the upsertion was successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean upsertObjectField(DatalakeStorageItem item, String key, Object value) throws DatalakeStorageException {
    return upsertObjectField(item.getIndex(), item.getId(), key, value);
  }

  /**
   * Insert or Update a specific field for an object.
   *
   * @param index ElasticSearch index.
   * @param id Item id.
   * @param key Key to upsert.
   * @param value Value to upsert.
   * @return True if the upsertion was successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean upsertObjectField(String index, String id, String key, Object value) throws DatalakeStorageException {
    boolean result = false;
    try {
      JSONObject jsonValue = new JSONObject();
      jsonValue.put("value", value);
      jsonValue.put("timestamp", generateDate());

      JSONObject scriptParam = new JSONObject();
      scriptParam.put("source", "ctx._source." + key + " = params.value; ctx._source['@timestamp'] = params.timestamp");
      scriptParam.put("lang", "painless");
      scriptParam.put("params", jsonValue);

      JSONObject messageBody = new JSONObject();
      messageBody.put("script", scriptParam);

      HttpResponse res = Unirest.post(String.format("%s/%s/_update/%s?retry_on_conflict=10", elasticSearchUrl, index, id)).header("Content-Type", "application/json").body(messageBody).basicAuth(elasticSearchUser, elasticSearchPassword).asEmpty();
      if (res.getStatus() != DatalakeStorageResponseCode.HTTP_OK) {
        LogManager.getLogger(DatalakeStorageItem.class).info(String.format("UpsertField (%s) from index %s status : %d body : %s", id, index, res.getStatus(), res.getBody()));
      }
      result = (res.getStatus() == DatalakeStorageResponseCode.HTTP_OK);
    } catch (Exception ex) {
      LogManager.getLogger(DatalakeStorageItem.class).error(String.format("UpsertField (%s) from index %s (Key : %s) (Value : %s) : %s", id, index, key, value, ex.getMessage()));
      throw new DatalakeStorageException(String.format("UpsertField (%s) from index %s (Key : %s) (Value : %s) : %s", id, index, key, value, ex.getMessage()), ex);
    }
    return result;
  }

  /**
   * Remove a specific field for an object.
   *
   * @param item Item model to upsert.
   * @param key Key to delete.
   * @return True if the deletion was successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public static boolean removeObjectField(DatalakeStorageItem item, String key) throws DatalakeStorageException {
    boolean result = false;

    try {
      JSONObject jsonValue = new JSONObject();
      jsonValue.put("timestamp", generateDate());

      JSONObject scriptParam = new JSONObject();
      scriptParam.put("source", "ctx._source.remove('" + key + "'); ctx._source['@timestamp'] = params.timestamp");
      scriptParam.put("lang", "painless");
      scriptParam.put("params", jsonValue);

      JSONObject messageBody = new JSONObject();
      messageBody.put("script", scriptParam);

      HttpResponse res = Unirest.post(String.format("%s/%s/_update/%s?retry_on_conflict=10", elasticSearchUrl, item.getIndex(), item.getId())).header("Content-Type", "application/json")
          .body(messageBody).basicAuth(elasticSearchUser, elasticSearchPassword).asEmpty();
      if (res.getStatus() != DatalakeStorageResponseCode.HTTP_OK) {
        LogManager.getLogger(DatalakeStorageItem.class).info(String.format("RemoveField (%s) from index %s status : %d body : %s", item.getId(), item.getIndex(), res.getStatus(), res.getBody()));
      }
      result = (res.getStatus() == DatalakeStorageResponseCode.HTTP_OK);

    } catch (Exception ex) {
      LogManager.getLogger(DatalakeStorageItem.class).error(String.format("RemoveField (%s) from index %s (Key : %s) : %s", item.getId(), item.getIndex(), key, ex.getMessage()));
      throw new DatalakeStorageException(String.format("RemoveField (%s) from index %s (Key : %s) : %s", item.getId(), item.getIndex(), key, ex.getMessage()), ex);
    }
    return result;
  }

  /**
   * Generate date in Elastic Search format.
   *
   * @return Date formatted for Elastic Search.
   */
  private static String generateDate() {
    return simpleDateFormat.format(new Date());
  }
}
