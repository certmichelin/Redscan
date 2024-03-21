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

package com.michelin.cert.redscan;

import com.michelin.cert.redscan.utils.caching.CacheManager;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configure Cache manager.
 *
 * @author Maxime ESCOURBIAC
 */
@Configuration
public class CacheConfig extends CacheManager {

  @Value("${cache.manager.url}")
  private String cacheManagerUrlProperty;

  @Autowired
  public CacheConfig() {
  }

  @PostConstruct
  public void initCacheManager() {
    cacheManagerUrl = cacheManagerUrlProperty;
    applicationName = "redscan-urlcrazy";
  }

}
