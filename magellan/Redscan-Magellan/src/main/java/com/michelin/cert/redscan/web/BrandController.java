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

import com.michelin.cert.redscan.service.BrandService;
import com.michelin.cert.redscan.utils.models.Brand;

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
 * Brand controller.
 *
 * @author Maxime ESCOURBIAC
 */
@RestController
@RequestMapping("/rest/brands")
public class BrandController {

  @Autowired
  BrandService brandService;

  /**
   * Default constructor.
   */
  @Autowired
  public BrandController() {
  }

  /**
   * Get all brands.
   *
   * @return All brands.
   */
  @GetMapping()
  public List<Brand> findAll() {
    return brandService.findAll();
  }

  /**
   * Export all brands.
   *
   * @return Export all brands.
   */
  @GetMapping("/export")
  public HttpEntity<List<Brand>> export() {
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_JSON);
    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=brands.json");
    return new HttpEntity<>(brandService.findAll(), header);
  }

  /**
   * Import brands.
   *
   * @param brands Brands to import.
   * @return Number of brands well imported.
   */
  @PostMapping("/import")
  public int upload(@RequestBody ArrayList<Brand> brands) {
    int upsertedCount = 0;
    if (brands != null) {
      for (Brand brand : brands) {
        if (brandService.find(brand) == null) {
          if (brandService.create(brand)) {
            ++upsertedCount;
          }
        } else {
          if (brandService.update(brand)) {
            ++upsertedCount;
          }
        }
      }
    }
    return upsertedCount;
  }

  /**
   * Create a brand.
   *
   * @param brand Brand to create.
   * @return True if the creation succeed.
   */
  @PostMapping
  public boolean create(@RequestBody Brand brand) {
    return brandService.create(brand);
  }

  /**
   * Delete a brand.
   *
   * @param name Brand name.
   * @return True if the entity is well deleted.
   */
  @DeleteMapping("/{name}")
  public boolean delete(@PathVariable String name) {
    Brand brand = new Brand(name);
    return brandService.delete(brand);
  }

  /**
   * Ventilate all brands.
   *
   * @return True if the ventilation succeeded.
   */
  @PostMapping("/ventilate")
  public boolean ventilate() {
    return brandService.ventilate();
  }

}
