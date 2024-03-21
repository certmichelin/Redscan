/*
 * Copyright 2023 Michelin CERT (https://cert.michelin.com/)
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

package com.michelin.cert.redscan.api;

import com.michelin.cert.redscan.service.DomainService;
import com.michelin.cert.redscan.utils.models.Domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Domain controller.
 *
 * @author Axel REMACK
 */
@RestController
@RequestMapping("/api/domains")
public class DomainController extends DatalakeStorageItemController<Domain> {

  @Autowired
  public void setService(DomainService service) {
    this.service = service;
  }

  /**
   * Default constructor.
   */
  @Autowired
  public DomainController() { }

}
