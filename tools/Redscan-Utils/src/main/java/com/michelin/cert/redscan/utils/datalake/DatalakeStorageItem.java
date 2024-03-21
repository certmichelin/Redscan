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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

/**
 * API for interacting with ElasticSearch.
 *
 * @author Florent BORDIGNON
 * @author Maxime ESCOURBIAC
 * @author Maxence SCHMITT
 * @author Axel REMACK
 */
public abstract class DatalakeStorageItem {

  @JsonIgnore
  private static final String BLOCK_INDEX = "blocklist";

  @JsonIgnore
  protected String index;

  protected String parent;

  protected boolean blocked;
  protected Map<String,Object> data;

  @JsonFormat(pattern = "yyyy-MM-dd kk:mm")
  private Date lastScanDate;

  @JsonIgnore
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");

  /**
   * DatalakeStorageItem default constructor.
   */
  public DatalakeStorageItem() {
    index = null;
    parent = null;
  }

  /**
   * Create the item in Elastic searchContent.
   *
   * @return True if the creation is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public boolean create() throws DatalakeStorageException {
    boolean result = DatalakeStorage.createObject(this);

    if (this.getBlocked()) {
      result &= this.block();
    }

    result &= upsert();

    return result;
  }

  /**
   * Upsert the item in Elastic searchContent. This operation cannot be
   * mutualized and must be defined in each children classes.
   *
   * @return True if the upsert is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public abstract boolean upsert() throws DatalakeStorageException;

  /**
   * Delete the item from ElasticSearch.
   *
   * @return True if the deletion is successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public boolean delete() throws DatalakeStorageException {
    boolean result = true;
    if (this.isBlocked()) {
      result &= this.unblock();
    }
    result &= DatalakeStorage.deleteObject(this);
    return result;
  }

  /**
   * Insert or Update a specific field for the item.
   *
   * @param key Key to upsert.
   * @param value Value to upsert.
   * @return True if the upsertion was successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public boolean upsertField(String key, Object value) throws DatalakeStorageException {
    return DatalakeStorage.upsertObjectField(this, key, value);
  }

  /**
   * Delete a specific field for the item.
   *
   * @param key Key to delete.
   * @return True if the deletion was successful.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public boolean removeField(String key) throws DatalakeStorageException {
    return DatalakeStorage.removeObjectField(this, key);
  }

  /**
   * Get all instanciated items.
   *
   * @param <T> DataStorageItem
   * @return List of all instanciated items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> findAll() throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = findAllContent();
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Get all instanciated items with pagination.
   *
   * @param <T> DataStorageItem
   * @param page Page number.
   * @param size Number of items in each page.
   * @return List of all instanciated items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> findAll(String page, String size) throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = findAllContent(page, size);
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Get all instanciated items with particular sorting.
   *
   * @param <T> DataStorageItem.
   * @param sort Elastic sort query.
   * @return List of all instanciated items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> findAll(String sort) throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = findAllContent(new JSONObject(sort));
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Get all instanciated items with pagination and particular sorting.
   *
   * @param <T> DataStorageItem.
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of items in each page.
   * @return List of all instanciated items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> findAll(String sort, String page, String size) throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = findAllContent(new JSONObject(sort), page, size);
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Get all items.
   *
   * @return All items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  protected JSONObject findAllContent() throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, null);
  }

  /**
   * Get all items with particular sorting.
   *
   * @param sort Elastic sort query.
   * @return All items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  protected JSONObject findAllContent(JSONObject sort) throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, null, sort);
  }

  /**
   * Get all items with pagination.
   *
   * @param page Page number.
   * @param size Number of items in each page.
   * @return All items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public JSONObject findAllContent(String page, String size) throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, null, page, size);
  }

  /**
   * Get all items with pagination and particular sorting.
   *
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of items in each page.
   * @return All items.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public JSONObject findAllContent(JSONObject sort, String page, String size) throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, null, page, size, sort);
  }

  /**
   * Get instantiated item.
   *
   * @param <T> DataStorageItem
   * @return Item by id.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> T find() throws DatalakeStorageException {
    return find(getId());
  }

  /**
   * Get instantiated item.
   *
   * @param <T> DataStorageItem.
   * @param id Specific ID.
   * @return Item by id.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> T find(String id) throws DatalakeStorageException {
    JSONObject jsonObject = this.findContent(id);
    T result = this.fromDatalake(jsonObject);
    if (result != null) {
      result.setData(jsonObject.toMap());
    }
    return result;
  }
  
  /**
   * Search items.
   *
   * @param <T> DataStorageItem
   * @param query Elastic searchContent query.
   * @return DataStorageItem instanciated corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> search(JSONObject query) throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = this.searchContent(query);
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Search items with sorting.
   *
   * @param <T> DataStorageItem
   * @param query Elastic searchContent query.
   * @param sort Elastic sort query.
   * @return DataStorageItem instanciated corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> search(JSONObject query, String sort) throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = this.searchContent(query, new JSONObject(sort));
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Search items with pagination.
   *
   * @param <T> DataStorageItem
   * @param page Page number.
   * @param size Number of items in each page.
   * @param query Elastic searchContent query.
   * @return DataStorageItem instanciated corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> search(String page, String size, JSONObject query) throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = this.searchContent(query, page, size);
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Search items with pagination and sorting.
   *
   * @param <T> DataStorageItem
   * @param page Page number.
   * @param size Number of items in each page.
   * @param query Elastic searchContent query.
   * @param sort Elastic sort query.
   * @return DataStorageItem instanciated corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public <T extends DatalakeStorageItem> List<T> search(String page, String size, JSONObject query, String sort) throws DatalakeStorageException {
    List<T> datalakeStorageItems = new ArrayList<>();
    JSONObject datalakeObject = this.searchContent(query, page, size, new JSONObject(sort));
    if (datalakeObject != null) {
      int nbHits = datalakeObject.getJSONObject("total").getInt("value");
      JSONArray hits = datalakeObject.getJSONArray("hits");
      for (int i = 0; i < nbHits; ++i) {
        datalakeStorageItems.add(this.fromDatalake(hits.getJSONObject(i).getJSONObject("_source")));
      }
    }
    return datalakeStorageItems;
  }

  /**
   * Get item from their id.
   *
   * @param id Specific ID.
   * @return Item by id.
   * @throws DatalakeStorageException Exception with the storage.
   */
  protected JSONObject findContent(String id) throws DatalakeStorageException {
    return (id == null) ? DatalakeStorage.getObject(this) : DatalakeStorage.getObject(this, id);
  }

  /**
   * Search items.
   *
   * @param query Elastic searchContent query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  protected JSONObject searchContent(JSONObject query) throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, query);
  }

  /**
   * Search items with sorting.
   *
   * @param query Elastic searchContent query.
   * @param sort Elastic sort query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  protected JSONObject searchContent(JSONObject query, JSONObject sort) throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, query, sort);
  }

  /**
   * Search items with pagination.
   *
   * @param query Elastic searchContent query.
   * @param page Page number.
   * @param size Number of items in each page.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  protected JSONObject searchContent(JSONObject query, String page, String size) throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, query, page, size);
  }

  /**
   * Search items with pagination and sorting.
   *
   * @param query Elastic searchContent query.
   * @param page Page number.
   * @param size Number of items in each page.
   * @param sort Elastic sort query.
   * @return Objects corresponding to the query.
   * @throws DatalakeStorageException Exception with the storage.
   */
  protected JSONObject searchContent(JSONObject query, String page, String size, JSONObject sort) throws DatalakeStorageException {
    return DatalakeStorage.getObjects(this, query, page, size, sort);
  }

  /**
   * Item ID.
   *
   * @return Item ID.
   */
  public abstract String getId();

  /**
   * Block ID. This id is used for Block List feature.
   *
   * @return Block ID.
   */
  public String getBlockId() {
    return this.index + "@" + getId();
  }

  /**
   * Determine if Datalake storage item is blocked.
   *
   * @return True if Datalake storage item is blocked.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public boolean isBlocked() throws DatalakeStorageException {
    return (DatalakeStorage.getObject(BLOCK_INDEX, getBlockId()) != null);
  }

  /**
   * Block the Datalake storage item.
   *
   * @return True if Datalake storage item was blocked.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public boolean block() throws DatalakeStorageException {
    setBlocked(true);
    boolean result = upsert();
    result &= DatalakeStorage.createObject(BLOCK_INDEX, getBlockId());
    return result;
  }

  /**
   * Unblock the Datalake storage item.
   *
   * @return True if Datalake storage item was unblocked.
   * @throws DatalakeStorageException Exception with the storage.
   */
  public boolean unblock() throws DatalakeStorageException {
    setBlocked(false);
    boolean result = upsert();
    result &= DatalakeStorage.deleteObject(BLOCK_INDEX, getBlockId());
    return result;
  }

  /**
   * Get index name. (ex: brands)
   *
   * @return Index name.
   */
  public String getIndex() {
    return index;
  }

  /**
   * Index name.
   *
   * @param index Index name.
   */
  public void setIndex(String index) {
    this.index = index;
  }

  /**
   * Parent item id.
   *
   * @return Parent item id.
   */
  public String getParent() {
    return parent;
  }

  /**
   * Parent item id.
   *
   * @param parent Parent item id.
   */
  public void setParent(String parent) {
    this.parent = parent;
  }

  /**
   * Item's blocking status.
   *
   * @return Item's blocking status.
   */
  public boolean getBlocked() {
    return blocked;
  }

  /**
   * Item's blocking status.
   *
   * @param blocked Item's blocking status.
   */
  public void setBlocked(boolean blocked) {
    this.blocked = blocked;
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
   * Convert String to Date.
   *
   * @param str String to convert.
   * @return String converted in Date.
   */
  public static Date toDate(String str) {
    Date date = null;
    if (str != null && !str.isEmpty()) {
      try {
        date = sdf.parse(str);
      } catch (ParseException ex) {
        LogManager.getLogger(DatalakeStorageItem.class).error(ex.getMessage());
      }
    }
    return date;
  }

  /**
   * Convert Date to String.
   *
   * @param date Date to convert.
   * @return Date converted in String.
   */
  public static String fromDate(Date date) {
    return (date != null) ? sdf.format(date) : null;
  }

  /**
   * All data from elasticsearch for a datalake storage item.
   *
   * @return All data from elasticsearch for a datalake storage item.
   */
  public Map<String, Object> getData() {
    return data;
  }

  /**
   * All data from elasticsearch for a datalake storage item.
   *
   * @param data All data from elasticsearch for a datalake storage item.
   */
  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  /**
   * Create DatalakeStorageItem instance from Datalake data. Be careful to
   * verify if the JSON object parameter is not null. If object is null =>
   * return null.
   *
   * @param <T> DatalakeStorageItem children.
   * @param object Datalake data.
   * @return DatalakeStorageItem instantiated.
   */
  protected abstract <T extends DatalakeStorageItem> T fromDatalake(JSONObject object);
}
