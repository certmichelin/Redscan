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
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Cache management.
 *
 * @author Maxime ESCOURBIAC
 */
public class CacheManagerTest {

  public CacheManagerTest() {
  }

  /**
   * Test of buildKey method, of class CacheManager.
   */
  @Test
  public void testBuildKey() {
    System.out.println("CacheManager:BuildKey");
    CacheManager instance = new CacheManager("TEST", "blah");
    String expResult = "TEST_aaa_bbb";
    String result = instance.buildKey("aaa", "bbb");
    assertEquals(expResult, result);
  }

  /**
   * Test of getCache method, of class CacheManager.
   */
  @Test
  public void testCache() {
    System.out.println("CacheManager:Set");
    CacheManager cacheManager = new CacheManager("TEST", "http://127.0.0.1:8080");
    Map<String, String> values = new HashMap<>();
    values.put("aaa", "ccc");
    values.put("bbb", "ddd");
    assertTrue(cacheManager.setCache("key1", values));
    assertFalse(cacheManager.setCache("toolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolo"
            + "ngtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolon"
            + "gtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolong"
            + "toolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongt"
            + "oolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongto"
            + "olongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoo"
            + "olongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoo"
            + "longtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtool"
            + "ongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolo"
            + "ngtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolon"
            + "gtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolong"
            + "toolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongt"
            + "oolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongto"
            + "olongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoo"
            + "longtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtoolongtool"
            + "ongtoolongtoolongtoolongtoolongtoolongtoolong", values));

    System.out.println("CacheManager:Get");
    Map<String, String> cache = cacheManager.getCache("key1", 5);
    assertNotNull(cache);
    assertEquals(2, cache.size());
    assertEquals("ccc", cache.get("aaa"));
    assertEquals("ddd", cache.get("bbb"));
  }

}
