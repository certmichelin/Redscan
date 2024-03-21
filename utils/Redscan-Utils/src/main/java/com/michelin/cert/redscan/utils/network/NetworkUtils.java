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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;

/**
 * Network utils class.
 *
 * @author Maxime ESCOURBIAC
 */
public class NetworkUtils {

  /**
   * Test if the domain link to localhost.
   *
   * @param domain Domain to verify.
   * @return True if the domain link to localhost.
   */
  public static boolean isLocalhost(String domain) {
    boolean result = false;
    if (domain != null) {
      try {
        result = InetAddress.getByName(domain).isAnyLocalAddress() || InetAddress.getByName(domain).isLoopbackAddress();
      } catch (UnknownHostException ex) {
        LogManager.getLogger(NetworkUtils.class).info(String.format("Unknown host : %s", domain));
      } catch (Exception ex) {
        LogManager.getLogger(NetworkUtils.class).error(String.format("Exception : %s", ex.getMessage()));
      }
    }
    return result;
  }

  /**
   * Test if the domain link to internal address.
   *
   * @param domain Domain to verify.
   * @return True if the domain link to internal address.
   */
  public static boolean isLocal(String domain) {
    boolean result = false;
    if (domain != null) {
      try {
        result = InetAddress.getByName(domain).isLinkLocalAddress();
      } catch (UnknownHostException ex) {
        LogManager.getLogger(NetworkUtils.class).info(String.format("Unknown host : %s", domain));
      } catch (Exception ex) {
        LogManager.getLogger(NetworkUtils.class).error(String.format("Exception : %s", ex.getMessage()));
      }
    }
    return result;
  }
}
