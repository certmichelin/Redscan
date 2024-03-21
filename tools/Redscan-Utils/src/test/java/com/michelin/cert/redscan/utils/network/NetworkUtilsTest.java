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
package com.michelin.cert.redscan.utils.network;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the NetworkUtils.
 *
 * @author Maxime ESCOURBIAC.
 */
public class NetworkUtilsTest {

  public NetworkUtilsTest() {
  }

  /**
   * Test of isLocalhost method, of class NetworkUtils.
   */
  @Test
  public void testIsLocalhost() {
    System.out.println("NetworkUtils:IsLocalhost");
    assertTrue(NetworkUtils.isLocalhost("localhost"));
    assertTrue(NetworkUtils.isLocalhost("127.0.0.1"));
    assertTrue(NetworkUtils.isLocalhost("localtest.me"));
    assertFalse(NetworkUtils.isLocalhost("www.michelin.com"));
    assertFalse(NetworkUtils.isLocalhost("192.168.1.20"));
    assertFalse(NetworkUtils.isLocalhost("fghjfdkshfgkjfhgkjfdsh.michelin.com"));
  }

  /**
   * Test of isLocal method, of class NetworkUtils.
   */
  @Test
  public void testIsLocal() {
    System.out.println("NetworkUtils:IsLocal");

    assertFalse(NetworkUtils.isLocal("www.michelin.com"));
    assertFalse(NetworkUtils.isLocal("fghjfdkshfgkjfhgkjfdsh.michelin.com"));
  }

}
