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

import com.michelin.cert.redscan.utils.json.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 * DatalakeStorageItem test class.
 *
 * @author Maxime Escourbiac
 */
public class DatalakeStorageItemTest {

  private static final String DATALAKE_URL = "http://127.0.0.1:9200";
  private static final String DATALAKE_USER = "";
  private static final String DATALAKE_PASSWORD = "";
  private static final int NB_CREATION = 2100;

  public DatalakeStorageItemTest() {
  }

  @BeforeClass
  public static void init() throws DatalakeStorageException {
    DatalakeStorage.init(DATALAKE_URL, DATALAKE_USER, DATALAKE_PASSWORD);
    cleanUp();
  }

  @AfterClass
  public static void cleanUp() throws DatalakeStorageException {
    MockStorageItem mock1 = new MockStorageItem("test1", null);
    MockStorageItem mock2 = new MockStorageItem("test2", null);
    mock1.delete();
    mock2.delete();

    for (int i = 0; i < NB_CREATION; i++) {
      MockStorageItem mock = new MockStorageItem("testGetObjects" + i, null);
      mock.delete();
    }
  }

  /**
   * Test all methods, of class DatalakeStorageItem.
   */
  @Test
  public void testAll() {
    try {
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      System.out.println("DatalakeStorageItem:Create");
      MockStorageItem test1 = new MockStorageItem("test1", "parent1");
      MockStorageItem test2 = new MockStorageItem("test2", "parent2");
      MockStorageItem nonValidName = new MockStorageItem("test%\\0' 1", "test%\\0' 1");

      assertTrue(test1.create());
      assertTrue(test2.create());
      assertFalse(test1.create());

      try {
        nonValidName.create();
        fail("Exception should be triggered");
      } catch (DatalakeStorageException ex) {
        Logger.getLogger(DatalakeStorageItemTest.class.getName()).log(Level.INFO, null, ex);
      }

      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      System.out.println("DatalakeStorageItem:Update");
      assertTrue(test1.upsertField("test1", "value1"));
      assertTrue(test1.upsertField("test1", "value2"));
      assertTrue(test1.upsertField("test1", true));
      assertTrue(test1.upsertField("test1", new ArrayList<String>()));
      assertTrue(test1.upsertField("test2", "value1"));

      MockStorageItem notExistingItem = new MockStorageItem("testXXXX", null);
      assertFalse(notExistingItem.upsertField("test1", "value1"));

      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      System.out.println("DatalakeStorageItem:ReadContent");
      JSONObject domain = test1.findContent(null);
      assertNotNull(domain);
      assertEquals("value1", domain.getString("test2"));
      assertEquals("test1", domain.getString("id"));
      assertEquals("parent1", domain.getString("parent"));
      assertNull(notExistingItem.findContent(null));

      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      System.out.println("DatalakeStorageItem:Read");
      MockStorageItem mockStorageItem = test1.find();
      assertEquals("test1", mockStorageItem.getId());
      assertEquals("parent1", mockStorageItem.getParent());
      assertNull(notExistingItem.find());

      try {
        nonValidName.findContent(null);
        fail("Exception should be triggered");
      } catch (DatalakeStorageException ex) {
        Logger.getLogger(DatalakeStorageItemTest.class.getName()).log(Level.INFO, null, ex);
      }
      
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      System.out.println("DatalakeStorageItem:DeleteField");
      assertTrue(test1.removeField("test2"));
      domain = test1.findContent(null);
      try {
        domain.getString("test2"); //Should Trigger JSONException
        fail("Test2 Should not be present");
      } catch (JSONException ex) {
      }
      assertFalse(notExistingItem.removeField("testXXXXX"));

      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      //----------------------------------------------------------------------------------------------
      System.out.println("DatalakeStorageItem:Delete");
      assertTrue(test2.delete());
      assertFalse(notExistingItem.delete());
      domain = test2.findContent(null);
      assertNull(domain);

      try {
        nonValidName.delete();
        fail("Exception should be triggered");
      } catch (DatalakeStorageException ex) {
        Logger.getLogger(DatalakeStorageItemTest.class.getName()).log(Level.INFO, null, ex);
      }

    } catch (DatalakeStorageException ex) {
      Logger.getLogger(DatalakeStorageItemTest.class.getName()).log(Level.SEVERE, null, ex);
      fail("Exeption has been triggered!!!");
    }
  }

  /**
   * Test getAllContent of class DatalakeStorageItem.
   *
   * @throws DatalakeStorageException Should never happened.
   */
  @Test
  public void testgetAllContent() throws DatalakeStorageException {
    try {
      cleanUp();
      System.out.println("DatalakeStorageItem:getAllContent");

      // Create many entry
      for (int i = 0; i < NB_CREATION; i++) {
        MockStorageItem item = new MockStorageItem("testGetObjects" + i, "");
        assertTrue(item.create());
      }
      try {
        System.out.println("DatalakeStorageItem: Waiting for objects to be created in index");
        Thread.sleep(5000);

        MockStorageItem item = new MockStorageItem();
        JSONObject masterDomainsRaw = item.findAllContent();
        int nbHits = masterDomainsRaw.getJSONObject("total").getInt("value");
        JSONArray hits = masterDomainsRaw.getJSONArray("hits");

        assertTrue(NB_CREATION == hits.length());
        assertTrue(NB_CREATION == nbHits);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        fail("Sleep failed");
      }
    } catch (DatalakeStorageException ex) {
      Logger.getLogger(DatalakeStorageItemTest.class.getName()).log(Level.SEVERE, null, ex);
      fail("Exeption has been triggered!!!");
    }
  }

  /**
   * Test getObject of class DatalakeStorageItem.
   *
   * @throws DatalakeStorageException Should never happened.
   */
  @Test
  public void testGetAll() throws DatalakeStorageException {
    try {
      cleanUp();
      System.out.println("DatalakeStorageItem:GetAll");

      // Create many entry
      for (int i = 0; i < NB_CREATION; i++) {
        MockStorageItem item = new MockStorageItem("testGetObjects" + i, "ip" + i, "parent" + i);
        assertTrue(item.create());
      }
      try {
        System.out.println("DatalakeStorageItem: Waiting for objects to be created in index");
        Thread.sleep(5000);

        MockStorageItem item = new MockStorageItem();
        List<MockStorageItem> all = item.findAll();
        assertTrue(NB_CREATION == all.size());
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        fail("Sleep failed");
      }
    } catch (DatalakeStorageException ex) {
      Logger.getLogger(DatalakeStorageItemTest.class.getName()).log(Level.SEVERE, null, ex);
      fail("Exeption has been triggered!!!");
    }
  }

  /**
   * Test getObject of class DatalakeStorageItem.
   *
   * @throws DatalakeStorageException Should never happened.
   */
  @Test
  public void testBlockFeature() throws DatalakeStorageException {
    cleanUp();
    System.out.println("DatalakeStorageItem:Block");
    MockStorageItem test1 = new MockStorageItem("test1", "parent1");
    MockStorageItem test2 = new MockStorageItem("test2", "parent2");
    test1.create();
    test2.create();

    assertFalse(test1.isBlocked());
    assertFalse(test2.isBlocked());

    assertTrue(test1.block());
    assertTrue(test1.isBlocked());
    assertFalse(test2.isBlocked());
    assertFalse(test1.block());

    assertTrue(test1.unblock());
    assertFalse(test1.isBlocked());
    assertFalse(test2.isBlocked());
    assertFalse(test1.unblock());
  }

  /**
   * Sample Storage item for test.
   */
  public static class MockStorageItem extends DatalakeStorageItem {

    private String name;
    private String ip;
    private boolean blocked;

    public MockStorageItem() {
      this.index = "test";
    }

    public MockStorageItem(String name, String parent) {
      this();
      this.name = name;
      this.parent = parent;
    }

    public MockStorageItem(String name, String ip, String parent) {
      this(name, parent);
      this.ip = ip;
    }

    public MockStorageItem(String name, String parent, boolean blocked) {
      this(name, parent);
      this.blocked = blocked;
    }

    @Override
    public String getId() {
      return name;
    }

    public String getName() {
      return name;
    }

    public String getIp() {
      return ip;
    }

    public boolean getBlocked() {
      return blocked;
    }


    public void setName(String name) {
      this.name = name;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public void setBlocked(boolean blocked) {
      this.blocked = blocked;
    }


    @Override
    public <T extends DatalakeStorageItem> T fromDatalake(JSONObject object) {
      return (object == null) ? null : (T) new MockStorageItem(
          JsonUtils.getSafeString(object, "name"),
          JsonUtils.getSafeString(object, "ip"),
          JsonUtils.getSafeString(object, "parent"));
    }

    @Override
    public boolean upsert() throws DatalakeStorageException {
      boolean result = (find() != null);
      if (result) {
        result &= this.upsertField("name", name);
        result &= this.upsertField("ip", ip);
        result &= this.upsertField("blocked", blocked);
      }
      return result;
    }

  }

}
