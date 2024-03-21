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

package com.michelin.cert.redscan.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Temporal;

/**
 * CacheEntry model.
 *
 * @author Maxime ESCOURBIAC
 */
@Entity
public class CacheEntry implements Serializable {

  @Id
  @GeneratedValue
  private Long id;

  @Column(unique = true, columnDefinition = "VARCHAR(2000)")
  private String cacheKey;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "cache_mapping",
          joinColumns = {
            @JoinColumn(name = "cache_id", referencedColumnName = "id")})
  @MapKeyColumn(name = "cache_name")
  @Column(name = "cache_values", columnDefinition = "LONGTEXT")
  private Map<String, String> cacheMap;

  @Column(nullable = false)
  @Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private Date inserted;

  /**
   * Default constructor.
   */
  public CacheEntry() {
  }

  /**
   * Cache entry id.
   *
   * @return Cache entry id.
   */
  public Long getId() {
    return id;
  }

  /**
   * Cache entry id.
   *
   * @param id Cache entry id.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Cache entry key.
   *
   * @return Cache entry key.
   */
  public String getCacheKey() {
    return cacheKey;
  }

  /**
   * Cache entry key.
   *
   * @param cacheKey Cache entry key.
   */
  public void setCacheKey(String cacheKey) {
    this.cacheKey = cacheKey;
  }

  /**
   * Cache entry values.
   *
   * @return Cache entry values.
   */
  public Map<String, String> getCacheMap() {
    return cacheMap;
  }

  /**
   * Cache entry values.
   *
   * @param cacheMap Cache entry values.
   */
  public void setCacheMap(Map<String, String> cacheMap) {
    this.cacheMap = cacheMap;
  }

  /**
   * Cache entry insertion date.
   *
   * @return Cache entry insertion date.
   */
  public Date getInserted() {
    return inserted;
  }

  /**
   * Cache entry insertion date.
   *
   * @param inserted Cache entry insertion date.
   */
  public void setInserted(Date inserted) {
    this.inserted = inserted;
  }

}
