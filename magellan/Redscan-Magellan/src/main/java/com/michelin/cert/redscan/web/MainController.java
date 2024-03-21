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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Application main controller.
 *
 * @author FP14265 - Maxime ESCOURBIAC
 * @author Maxence SCHMITT
 */
@Controller
public class MainController {

  /**
   * Default constructor.
   */
  @Autowired
  public MainController() {
  }

  /**
   * Display index page.
   *
   * @return template name.
   */
  @GetMapping("/")
  public String index() {
    return "masterdomains";
  }
  
  /**
   * Display brands page.
   *
   * @return template name.
   */
  @GetMapping("/brands")
  public String brands() {
    return "brands";
  }
  
  /**
   * Display ip ranges page.
   *
   * @return template name.
   */
  @GetMapping("/ipranges")
  public String ipranges() {
    return "ipranges";
  }
  
  /**
   * Display masterdomains page.
   *
   * @return template name.
   */
  @GetMapping("/masterdomains")
  public String masterdomains() {
    return "masterdomains";
  }
  
  /**
   * Display the import page.
   *
   * @return template name.
   */
  @GetMapping("/import")
  public String upload() {
    return "import";
  }

}
