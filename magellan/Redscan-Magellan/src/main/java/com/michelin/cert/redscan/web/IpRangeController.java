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

import com.michelin.cert.redscan.service.IpRangeService;
import com.michelin.cert.redscan.utils.models.IpRange;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * IpRange controller.
 *
 * @author Maxime ESCOURBIAC
 */

@RestController
@RequestMapping("/rest/ipranges")
public class IpRangeController {

  @Autowired
  IpRangeService ipRangeService;


  /**
   * Default constructor.
   */
  @Autowired
  public IpRangeController() {
  }

  /**
   * Get all ipRanges.
   *
   * @return All ipRanges.
   */
  @GetMapping()
  public List<IpRange> findAll() {
    return ipRangeService.findAll();
  }

  /**
   * Export all ipRanges.
   *
   * @return Export all ipRanges.
   */
  @GetMapping("/export")
  public HttpEntity<List<IpRange>> export() {
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_JSON);
    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ipRanges.json");
    return new HttpEntity<>(ipRangeService.findAll(), header);
  }

  /**
   * Import ipRanges.
   *
   * @param ipRanges IpRanges to import.
   * @return Number of ipRanges well imported.
   */
  @PostMapping("/import")
  public int upload(@RequestBody ArrayList<IpRange> ipRanges) {
    int upsertedCount = 0;
    if (ipRanges != null) {
      for (IpRange ipRange : ipRanges) {
        if (ipRangeService.find(ipRange) == null) {
          if (ipRangeService.create(ipRange)) {
            ++upsertedCount;
          }
        } else {
          if (ipRangeService.update(ipRange)) {
            ++upsertedCount;
          }
        }
      }
    }
    return upsertedCount;
  }

  /**
   * Create an ipRange.
   *
   * @param ipRange IpRange to create.
   * @return True if the creation succeed.
   */
  @PostMapping
  public boolean create(@RequestBody IpRange ipRange) {
    return ipRange.isValid() ? ipRangeService.create(ipRange) : false;
  }

  /**
   * Delete an ipRange.
   *
   * @param cidr IpRange name.
   * @return True if the entity is well deleted.
   */
  @DeleteMapping("/{cidr}")
  public boolean delete(@PathVariable String cidr) {
    IpRange ipRange = new IpRange(cidr.replace("_", "/"));
    return ipRangeService.delete(ipRange);
  }

  /**
   * Ventilate all IPRanges.
   *
   * @return True if the ventilation succeeded.
   */
  @PostMapping("/ventilate")
  public boolean ventilate() {
    return ipRangeService.ventilate();
  }


}

