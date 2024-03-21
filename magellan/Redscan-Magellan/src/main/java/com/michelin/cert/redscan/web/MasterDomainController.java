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

import com.michelin.cert.redscan.service.MasterDomainService;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.models.MasterDomain;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MasterDomain controller.
 *
 * @author Maxime ESCOURBIAC
 * @author Maxence SCHMITT
 */
@RestController
@RequestMapping("/rest/masterdomains")
public class MasterDomainController {

  @Autowired
  MasterDomainService masterDomainService;

  /**
   * Default constructor.
   */
  @Autowired
  public MasterDomainController() {
  }

  /**
   * Get all master domains.
   *
   * @return All master domains.
   */
  @GetMapping()
  public List<MasterDomain> findAll() {
    return masterDomainService.findAll();
  }

  /**
   * Export all masterdomains.
   *
   * @return Export all master domains.
   */
  @GetMapping("/export")
  public HttpEntity<List<MasterDomain>> export() {
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_JSON);
    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=masterdomains.json");
    return new HttpEntity<>(masterDomainService.findAll(), header);
  }

  /**
   * Import master domains.
   *
   * @param masterDomains Master domains to import.
   * @return Number of master domains well imported.
   */
  @PostMapping("/import")
  public int upload(@RequestBody ArrayList<MasterDomain> masterDomains) {
    int upsertedCount = 0;
    if (masterDomains != null) {
      for (MasterDomain masterDomain : masterDomains) {
        try {
          if (masterDomain.find() == null) {
            if (masterDomainService.create(masterDomain)) {
              ++upsertedCount;
            }
          } else {
            if (masterDomainService.update(masterDomain, false)) {
              ++upsertedCount;
            }
          }
        } catch (DatalakeStorageException ex) {
          LogManager.getLogger(MasterDomainController.class).error(String.format("MasterDomainController : Datalake storage exception %s", ex.getMessage()));
        }
      }
    }
    return upsertedCount;
  }

  /**
   * Get all in scope master domains.
   *
   * @return All in scope master domains.
   */
  @GetMapping("/inscope")
  public List<MasterDomain> findInScope() {
    return masterDomainService.getInScopeMasterDomains();
  }

  /**
   * Get all out of scope master domains.
   *
   * @return All out of scope master domains.
   */
  @GetMapping("/outofscope")
  public List<MasterDomain> findOutOfScope() {
    return masterDomainService.getNotInScopeMasterDomains();
  }

  /**
   * Get all master domains to review.
   *
   * @return All master domains to review.
   */
  @GetMapping("/toreview")
  public List<MasterDomain> findAllToReview() {
    return masterDomainService.getMasterDomainsToReview();
  }

  /**
   * Create a master domain.
   *
   * @param masterDomain Master domain to create.
   * @return True if the creation succeed.
   */
  @PostMapping
  public boolean create(@RequestBody MasterDomain masterDomain) {
    return masterDomainService.create(masterDomain);
  }

  /**
   * Change scope status of a master domain.
   *
   * @param name Master domain name.
   * @param inScope In scope status.
   * @return True if the update succeed.
   */
  @PutMapping("/{name}/scope/{inScope}")
  public boolean updateScope(@PathVariable String name, @PathVariable boolean inScope) {
    boolean updated = false;
    MasterDomain masterDomain = masterDomainService.find(new MasterDomain(name));
    if (masterDomain != null) {
      masterDomain.setInScope(inScope);
      masterDomain.setReviewed(true);
      updated = masterDomainService.update(masterDomain, true);
    }
    return updated;
  }

  /**
   * Change review status of a master domain.
   *
   * @param name Master domain name.
   * @param reviewed Review status
   * @return True if the update succeed.
   */
  @PutMapping("/{name}/review/{reviewed}")
  public boolean updateReview(@PathVariable String name, @PathVariable boolean reviewed) {
    boolean updated = false;
    MasterDomain masterDomain = masterDomainService.find(new MasterDomain(name));
    if (masterDomain != null) {
      masterDomain.setReviewed(reviewed);
      updated = masterDomainService.update(masterDomain, false);
    }
    return updated;
  }

  /**
   * Delete a master domain.
   *
   * @param name Master domain name.
   * @return True if the entity is well deleted.
   */
  @DeleteMapping("/{name}")
  public boolean delete(@PathVariable String name) {
    return masterDomainService.delete(new MasterDomain(name));
  }

  /**
   * Ventilate all master domains.
   *
   * @return True if the ventilation succeedded.
   */
  @PostMapping("/ventilate")
  public boolean ventilate() {
    return masterDomainService.ventilate();
  }

}
