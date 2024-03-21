
/**
 * Michelin CERT 2021.
 */
package com.michelin.cert.redscan.service;

import com.michelin.cert.redscan.domain.CacheEntry;
import com.michelin.cert.redscan.domain.CacheEntryRepository;
import java.util.Calendar;
import java.util.Date;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * CacheNotFoundAdvice.
 *
 * @author Maxime ESCOURBIAC
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheServiceTest {

  @Autowired
  private CacheService cacheService;

  @Autowired
  private CacheEntryRepository cacheEntryRepository;

  public CacheServiceTest() {
  }

  /**
   * Test cache management methods.
   */
  @Test
  public void testGetCacheManagement() {
    System.out.println("CacheService:Tests");

    String key1 = "key1";
    String key2 = "key2";
    String key3 = "LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey_LongKey";

    Map<String, String> sampleCache = new HashMap<>();
    sampleCache.put("aaa", "aaaa");
    sampleCache.put("long_data", "It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong It'sveryLong ");

    assertEquals(0, cacheService.getCacheSize());

    //Cache
    CacheEntry cacheEntry = new CacheEntry();
    cacheEntry.setCacheKey(key1);
    cacheEntry.setCacheMap(sampleCache);
    cacheService.postCache(cacheEntry);
    cacheEntry = new CacheEntry();
    cacheEntry.setCacheKey(key3);
    cacheEntry.setCacheMap(sampleCache);
    cacheService.postCache(cacheEntry);

    CacheEntry cache1 = cacheService.getCache(key1, 5);
    CacheEntry cache2 = cacheService.getCache(key1, -2);
    CacheEntry cache3 = cacheService.getCache(key1, 5);
    CacheEntry cache4 = cacheService.getCache(key2, 5);
    CacheEntry cache5 = cacheService.getCache(key3, 5);

    assertNotNull(cache1);
    assertNull(cache2);
    assertNull(cache3);
    assertNull(cache4);
    assertNotNull(cache5);

    assertEquals(key1, cache1.getCacheKey());
    assertEquals(2, cache1.getCacheMap().keySet().size());
    assertEquals(key3, cache5.getCacheKey());
    assertEquals(2, cache5.getCacheMap().keySet().size());
  }

  /**
   * Test flush cache method.
   */
  @Test
  public void testFlushCache() {
    System.out.println("CacheService:Flush");
    String flush_key1 = "flush_key1";
    String flush_key2 = "flush_key2";

    Date date_key1 = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.YEAR, -1);
    Date date_key2 = calendar.getTime();

    Map<String, String> map = new HashMap<>();
    
    CacheEntry cacheEntry1 = new CacheEntry();
    cacheEntry1.setCacheKey(flush_key1);
    cacheEntry1.setCacheMap(map);
    cacheEntry1.setInserted(date_key1);
    cacheEntryRepository.save(cacheEntry1);
    
    CacheEntry cacheEntry2 = new CacheEntry();
    cacheEntry2.setCacheKey(flush_key2);
    cacheEntry2.setCacheMap(map);
    cacheEntry2.setInserted(date_key2);
    cacheEntryRepository.save(cacheEntry2);
    
    assertNotNull(cacheEntryRepository.findOneByCacheKey(flush_key1));
    assertNotNull(cacheEntryRepository.findOneByCacheKey(flush_key2));
    
    cacheService.flush();
    
    assertNotNull(cacheEntryRepository.findOneByCacheKey(flush_key1));
    assertNull(cacheEntryRepository.findOneByCacheKey(flush_key2));
    
    cacheEntryRepository.delete(cacheEntryRepository.findOneByCacheKey(flush_key1));
  }

}
