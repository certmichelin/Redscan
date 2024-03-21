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

package com.michelin.cert.redscan.service;

import com.michelin.cert.redscan.domain.CacheEntry;
import com.michelin.cert.redscan.domain.CacheEntryRepository;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Cache service.
 *
 * @author Maxime ESCOURBIAC
 */
@Service
public class CacheService {

  @Autowired
  CacheEntryRepository cacheEntryRepository;

  /**
   * Default constructor.
   */
  public CacheService() {
  }

  /**
   * Get all exclusion patterns.
   *
   * @return All exclusion patterns.
   */
  public long getCacheSize() {
    return cacheEntryRepository.count();
  }

  /**
   * Get cache entry.
   *
   * @param key Cache key to retrieve.
   * @param validity validity in hours.
   * @return The retrieve cache.
   */
  public CacheEntry getCache(String key, int validity) {
    LogManager.getLogger(CacheService.class).info(String.format("Get Cache for : %s", key));
    CacheEntry cache = cacheEntryRepository.findOneByCacheKey(key);
    if (cache != null) {
      LogManager.getLogger(CacheService.class).info(String.format("Cache hit for : %s", key));
      //Check the cache validity
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(cache.getInserted());
      calendar.add(Calendar.HOUR_OF_DAY, validity);
      if (calendar.getTime().before(new Date())) {
        //the cache expired, it will be removed and return null.
        LogManager.getLogger(CacheService.class).info(String.format("Cache obsolete for : %s, it will be removed", key));
        cacheEntryRepository.delete(cache);
        cache = null;
      }
    } else {
      LogManager.getLogger(CacheService.class).info(String.format("Cache not found for : %s", key));
    }
    return cache;
  }

  /**
   * Cache an entry.
   *
   * @param cacheEntry Entry to cache.
   */
  public void postCache(CacheEntry cacheEntry) {
    if (cacheEntry != null) {
      if (cacheEntry.getCacheKey() != null) {
        LogManager.getLogger(CacheService.class).info(String.format("Set cache for : %s", cacheEntry.getCacheKey()));
        CacheEntry existingCache = cacheEntryRepository.findOneByCacheKey(cacheEntry.getCacheKey());
        if (existingCache != null) {
          LogManager.getLogger(CacheService.class).info(String.format("Update the cache for : %s", cacheEntry.getCacheKey()));
          existingCache.setInserted(new Date());
          existingCache.setCacheMap(cacheEntry.getCacheMap());
          cacheEntryRepository.save(existingCache);
        } else {
          LogManager.getLogger(CacheService.class).info(String.format("Create new cache for : %s", cacheEntry.getCacheKey()));
          //set the date & save.
          cacheEntry.setInserted(new Date());
          cacheEntryRepository.save(cacheEntry);
        }
      } else {
        LogManager.getLogger(CacheService.class).warn("Someone try to cache a null key");
      }
    } else {
      LogManager.getLogger(CacheService.class).warn("Someone try to cache a null entry");
    }

  }

  /**
   * Flush all cache entry having more than one week if not deleted by the scanner.
   */
  @Scheduled(cron = "${cache.flush.cron}")
  public void flush() {
    LogManager.getLogger(CacheService.class).info("Begin Flush cache operation");
    Iterable<CacheEntry> cacheEntries = cacheEntryRepository.findAll();
    if (cacheEntries != null) {
      //Manage the expiration date.
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      calendar.add(Calendar.WEEK_OF_YEAR, -1);
      for (CacheEntry cacheEntry : cacheEntries) {
        if (cacheEntry.getInserted().before(calendar.getTime())) {
          LogManager.getLogger(CacheService.class).info(String.format("Flush cache with key %s", cacheEntry.getCacheKey()));
          cacheEntryRepository.delete(cacheEntry);
        }
      }
      LogManager.getLogger(CacheService.class).info("End of flush cache operation");
    }
  }

}
