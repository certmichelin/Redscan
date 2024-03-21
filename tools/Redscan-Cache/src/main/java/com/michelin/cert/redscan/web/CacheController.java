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

package com.michelin.cert.redscan.web;

import com.michelin.cert.redscan.domain.CacheEntry;
import com.michelin.cert.redscan.service.CacheService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * Cache controller.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */
@RestController
@RequestMapping("/")
public class CacheController {

  @Autowired
  private CacheService cacheService;

  /**
   * Default constructor.
   */
  @Autowired
  public CacheController() {
  }

  /**
   * Get all brands.
   *
   * @return All brands.
   */
  @GetMapping()
  public String helloWorld() {
    return "Hello World from Cache Manager";
  }

  /**
   * Get cache size.
   *
   * @return Cache size.
   */
  @GetMapping("/count")
  public long count() {
    return cacheService.getCacheSize();
  }

  /**
   * Get cache entry.
   *
   * @param key Cache key to retrieve.
   * @param validity validity in hours.
   * @return The retrieve cache.
   */
  @GetMapping("/cache/{key}/{validity}")
  public CacheEntry getCache(@PathVariable("key") String key, @PathVariable("validity") int validity) {
    CacheEntry cache = cacheService.getCache(key, validity);
    if (cache == null) {
      throw new CacheNotFoundException(key);
    }
    return cache;
  }

  /**
   * Cache an entry.
   *
   * @param cacheEntry Entry to cache.
   */
  @PostMapping("/cache")
  public void postCache(@RequestBody CacheEntry cacheEntry) {
    cacheService.postCache(cacheEntry);
  }

  /**
   * Flush old entries.
   */
  @DeleteMapping("/cache")
  public void flush() {
    cacheService.flush();
  }

}
