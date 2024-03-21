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

package com.michelin.cert.redscan.utils.models;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * IpRange test class.
 *
 * @author Maxime Escourbiac
 */
public class IpRangeTest {
  
  public IpRangeTest() {
  }
  

  /**
   * Test of toIpList method, of class IpRange.
   */
  @Test
  public void testToIpList() {
    System.out.println("IpRange:toIpList");
    IpRange instance = new IpRange("195.212.111.240/29");
    List<String> ips = instance.toIpList();
    assertEquals(8, ips.size());
    assertTrue(ips.contains("195.212.111.240"));
    assertTrue(ips.contains("195.212.111.241"));
    assertTrue(ips.contains("195.212.111.242"));
    assertTrue(ips.contains("195.212.111.243"));
    assertTrue(ips.contains("195.212.111.244"));
    assertTrue(ips.contains("195.212.111.245"));
    assertTrue(ips.contains("195.212.111.246"));
    assertTrue(ips.contains("195.212.111.247"));
    
    instance = new IpRange("195.212.111.240/32");
    ips = instance.toIpList();
    assertEquals(1, ips.size());
    
    instance = new IpRange("195.212.111.240");
    ips = instance.toIpList();
    assertNull(ips);
    
    instance = new IpRange("blahblah");
    ips = instance.toIpList();
    assertNull(ips);
  }
  
  /**
   * Test of isValid method, of class IpRange.
   */
  @Test
  public void testIsValid() {
    System.out.println("IpRange:isValid");
    IpRange instance = new IpRange("195.212.111.240/29");
    assertTrue(instance.isValid());
    
    instance = new IpRange("195.212.111.240/32");
    assertTrue(instance.isValid());
    
    instance = new IpRange("195.212.111.240");
    assertFalse(instance.isValid());
    
    instance = new IpRange("blahblah-1");
    assertFalse(instance.isValid());
  }

 
}
