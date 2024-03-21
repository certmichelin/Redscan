/*
 * Copyright 2023 Michelin CERT (https://cert.michelin.com/)
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

package com.michelin.cert.redscan.service;

import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.models.Sendable;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * API abstract service.
 *
 * @author Axel REMACK
 * @author Maxime ESCOURBIAC
 * @param <T> DatalakeStorageItem related to controller.
 */
public abstract class DatalakeStorageItemService<T extends DatalakeStorageItem & Sendable> {

  protected DatalakeStorageItem item;

  private final Class<T> datalakeStorageItemClass;

  @Autowired
  private final RabbitTemplate rabbitTemplate;

  /**
   * Default constructor.
   *
   * @param item DatalakeStorageItem.
   */
  public DatalakeStorageItemService(DatalakeStorageItem item) {
    this.item = item;
    this.datalakeStorageItemClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    this.rabbitTemplate = new RabbitTemplate();
  }

  /**
   * Getters.
   */
  public Class<T> getDatalakeStorageItemClass() {
    return datalakeStorageItemClass;
  }

  /**
   * Get sorting query.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @return Elastic sort query.
   */
  public String getSortQuery(String field, String order) {
    if (field.equals("last_scan_date")) {
      field = "last_scan_date.keyword"; // Modifying field for sorting (because of multi-fields)
    }

    String sortQuery = "{\"" + field + "\":\"" + order + "\"}";
    if ("serviceLevel".equalsIgnoreCase(field)) {
      sortQuery = "{\"" + field + "\":\"{\"order\":\"" + order + "\",\"type\":\"long\"}}";
    }

    return sortQuery;
  }

  /**
   * Get all datalake items of a defined type.
   *
   * @return All datalake items of a defined type.
   */
  public List<T> findAll() {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all %ss.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName()));
    List<T> items = null;
    try {
      items = item.findAll();
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Get all datalake items of a defined type with particular sorting.
   *
   * @param sort Elastic sort query.
   * @return All datalake items of a defined type.
   */
  public List<T> findAll(String sort) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all %ss with sorting %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), sort));
    List<T> items = null;
    try {
      items = item.findAll(sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Get all datalake items of a defined type with pagination.
   *
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All datalake items in a specific page.
   */
  public List<T> findAll(String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all %ss with pagination.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName()));
    List<T> items = null;
    try {
      items = item.findAll(page, size);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Get all datalake items of a defined type with pagination and particular sorting.
   *
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All datalake items in a specific page.
   */
  public List<T> findAll(String sort, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all %ss with pagination and sorting %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), sort));
    List<T> items = null;
    try {
      items = item.findAll(sort, page, size);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Get all datalake items with a name including the searched string.
   *
   * @param name String to search.
   * @return All datalake items with a name including the searched string.
   */
  public List<T> search(String name) {
    LogManager.getLogger(getClass()).info(String.format("%s : Search %ss with id including \"%s\".", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), name));
    List<T> items = null;
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(id:*" + name + "*)\"}}");
      items = item.search(query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Get all datalake items with a name including the searched string and with particular sorting.
   *
   * @param name String to search.
   * @param sort Elastic sort query.
   * @return All datalake items with a name including the searched string.
   */
  public List<T> search(String name, String sort) {
    LogManager.getLogger(getClass()).info(String.format("%s : Search %ss with id including \"%s\" and sorting %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), name, sort));
    List<T> items = null;
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(id:*" + name + "*)\"}}");
      items = item.search(query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Get all datalake items with a name including the searched string with pagination.
   *
   * @param name String to search.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All datalake items with a name including the searched string.
   */
  public List<T> search(String name, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Search %ss with id including \"%s\" with pagination.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), name));
    List<T> items = null;
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(id:*" + name + "*)\"}}");
      items = item.search(page, size, query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Get all datalake items with a name including the searched string with pagination and particular sorting.
   *
   * @param name String to search.
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All datalake items with a name including the searched string.
   */
  public List<T> search(String name, String sort, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Search %ss with id including \"%s\" with pagination and sorting %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), name, sort));
    List<T> items = null;
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(id:*" + name + "*)\"}}");
      items = item.search(page, size, query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return items;
  }

  /**
   * Find a datalake item.
   *
   * @param id Identifier of the datalake item to find.
   * @return Datalake item found.
   */
  public T find(String id) {
    LogManager.getLogger(IpRangeService.class).info(String.format("%s : Search %s with id %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), id));
    T found = null;
    if (id != null) {
      try {
        found = item.find(id);
      } catch (DatalakeStorageException ex) {
        LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
      }
    }
    return found;
  }

  /**
   * Create a datalake item.
   *
   * @param item DatalakeStorageItem to create.
   * @return True if the creation succeeded.
   */
  public boolean create(T item) {
    LogManager.getLogger(getClass()).info(String.format("%s : Create %s %s", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), (item != null) ? item.getId() : "null"));
    boolean created = false;
    if (item != null) {
      try {
        created = item.create();
        if (created) {
          created = item.upsert();
        }
      } catch (DatalakeStorageException ex) {
        LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
      }
    }
    return created;
  }

  /**
   * Delete a datalake item.
   *
   * @param id Identifier of DatalakeStorageItem to delete.
   * @return True if the entity is deleted.
   */
  public boolean delete(String id) {
    LogManager.getLogger(getClass()).info(String.format("%s : Delete %s with id %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), id));
    boolean success = false;
    try {
      T item = this.item.find(id);
      success = (item != null) && (item.delete());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return success;
  }

  /**
   * Update a datalake item.
   *
   * @param item DatalakeStorageItem to update.
   * @return True if the update succeeded.
   */
  public boolean update(T item) {
    LogManager.getLogger(getClass()).info(String.format("%s : Update %s with id %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), (item != null) ? item.getId() : "null"));
    boolean success = false;
    if (item != null) {
      try {
        success = (item.find() != null && item.upsert());
      } catch (DatalakeStorageException ex) {
        LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
      }
    }
    return success;
  }

  /**
   * Get all blocked datalake items of a defined type.
   *
   * @return All blocked datalake items of a defined type.
   */
  public List<T> findAllBlocked() {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all blocked %ss.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName()));
    List<T> items;
    List<T> itemsBlocked = new ArrayList<>();
    try {
      items = item.findAll();
      if (items != null) {
        for (T item : items) {
          if (item.isBlocked()) {
            itemsBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsBlocked;
  }

  /**
   * Get all blocked datalake items of a defined type with particular sorting.
   *
   * @param sort Elastic sort query.
   * @return All blocked datalake items of a defined type, sorted.
   */
  public List<T> findAllBlocked(String sort) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all blocked %ss sorted with %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), sort));
    List<T> items;
    List<T> itemsBlocked = new ArrayList<>();
    try {
      items = item.findAll(sort);
      if (items != null) {
        for (T item : items) {
          if (item.isBlocked()) {
            itemsBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsBlocked;
  }

  /**
   * Get all blocked datalake items of a defined type with pagination.
   *
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All blocked datalake items of a defined type in a specific page.
   */
  public List<T> findAllBlocked(String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all blocked %ss with pagination.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName()));
    List<T> items;
    List<T> itemsBlocked = new ArrayList<>();
    try {
      items = item.findAll(page, size);
      if (items != null) {
        for (T item : items) {
          if (item.isBlocked()) {
            itemsBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsBlocked;
  }

  /**
   * Get all blocked datalake items of a defined type with pagination and particular sorting.
   *
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All blocked datalake items of a defined type in a specific page, sorted.
   */
  public List<T> findAllBlocked(String sort, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all blocked %ss with pagination and sorting %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), sort));
    List<T> items;
    List<T> itemsBlocked = new ArrayList<>();
    try {
      items = item.findAll(sort, page, size);
      if (items != null) {
        for (T item : items) {
          if (item.isBlocked()) {
            itemsBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsBlocked;
  }

  /**
   * Get all non-blocked datalake items of a defined type.
   *
   * @return All non-blocked datalake items of a defined type.
   */
  public List<T> findAllNonBlocked() {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all non-blocked %ss.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName()));
    List<T> items;
    List<T> itemsNotBlocked = new ArrayList<>();
    try {
      items = item.findAll();
      if (items != null) {
        for (T item : items) {
          if (!item.isBlocked()) {
            itemsNotBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsNotBlocked;
  }

  /**
   * Get all non-blocked datalake items of a defined type with particular sorting.
   *
   * @param sort Elastic sort query.
   * @return All non-blocked datalake items of a defined type, sorted.
   */
  public List<T> findAllNonBlocked(String sort) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all non-blocked %ss sorted with %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), sort));
    List<T> items;
    List<T> itemsNotBlocked = new ArrayList<>();
    try {
      items = item.findAll(sort);
      if (items != null) {
        for (T item : items) {
          if (!item.isBlocked()) {
            itemsNotBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsNotBlocked;
  }

  /**
   * Get all non-blocked datalake items of a defined type with pagination.
   *
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All non-blocked datalake items of a defined type in a specific page.
   */
  public List<T> findAllNonBlocked(String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all non-blocked %ss with pagination.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName()));
    List<T> items;
    List<T> itemsNotBlocked = new ArrayList<>();
    try {
      items = item.findAll(page, size);
      if (items != null) {
        for (T item : items) {
          if (!item.isBlocked()) {
            itemsNotBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsNotBlocked;
  }

  /**
   * Get all non-blocked datalake items of a defined type with pagination and particular sorting.
   *
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All non-blocked datalake items of a defined type in a specific page, sorted.
   */
  public List<T> findAllNonBlocked(String sort, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get all non-blocked %ss with pagination and sorting %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), sort));
    List<T> items;
    List<T> itemsNotBlocked = new ArrayList<>();
    try {
      items = item.findAll(sort, page, size);
      if (items != null) {
        for (T item : items) {
          if (!item.isBlocked()) {
            itemsNotBlocked.add(item);
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsNotBlocked;
  }

  /**
   * Block a datalake items from being scanned.
   *
   * @param id Identifier of DatalakeStorageItem to block.
   * @return True if the block operation succeeded.
   */
  public boolean block(String id) {
    LogManager.getLogger(getClass()).info(String.format("%s : Block %s with id %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), (item != null) ? item.getId() : "null"));
    boolean success = false;
    try {
      T item = this.item.find(id);
      success = (item != null) && (item.block());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return success;
  }

  /**
   * Unblock a datalake items.
   *
   * @param id Identifier of DatalakeStorageItem to unblock.
   * @return True if the unblock operation succeeded.
   */
  public boolean unblock(String id) {
    LogManager.getLogger(getClass()).info(String.format("%s : Unblock %s with id %s.", getClass().getSimpleName(), this.datalakeStorageItemClass.getSimpleName(), (item != null) ? item.getId() : "null"));
    boolean success = false;
    try {
      T item = this.item.find(id);
      success = (item != null) && (item.unblock());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return success;
  }

  /**
   * Reinject datalake item in scan.
   *
   * @param id Identifier of DatalakeStorageItem to reinject.
   * @return True if reinjection succeeded.
   */
  public boolean reinject(String id) {
    LogManager.getLogger(getClass()).info(String.format("%s : Begin reinjection of %s with id %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), id));
    boolean success = true;
    try {
      T temp = this.item.find(id);
      if (temp != null) {
        rabbitTemplate.convertAndSend(temp.getFanoutExchangeName(), "", temp.toJson());
      } else {
        success = false;
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return success;
  }


}
