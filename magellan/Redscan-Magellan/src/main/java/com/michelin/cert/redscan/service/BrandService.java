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

package com.michelin.cert.redscan.service;

import com.michelin.cert.redscan.config.ScanConfig;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.models.Brand;
import com.michelin.cert.redscan.utils.models.ServiceLevel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kong.unirest.json.JSONObject;
import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


/**
 * Brand service.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */
@Service
public class BrandService {

  @Autowired
  private ScanConfig scanConfig;

  private final RabbitTemplate rabbitTemplate;

  /**
   * Default constructor
   *
   * @param rabbitTemplate Rabbit template.
   */
  public BrandService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * Check brand to reinject each hour.
   */
  @Scheduled(cron = "${magellan.brands.cron}")
  public void scan() {
    LogManager.getLogger(BrandService.class).info("BrandService : Begin Cron task");
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    try {
      List<Brand> brands = (new Brand()).findAll();
      for (Brand brand : brands) {
        if (brand.getLastScanDate() != null) {
          calendar.setTime(brand.getLastScanDate());
        } else {
          calendar.setTime(new Date(0));
        }

        //Apply service level.
        int period = getServiceLevelPeriod(ServiceLevel.findByValue(brand.getServiceLevel()));

        if (period != -1) {
          calendar.add(Calendar.DATE, period);
          if (calendar.getTime().compareTo(now) < 0) {
            LogManager.getLogger(BrandService.class).info(String.format("BrandService : Reinject %s in the matrix", brand.getName()));
            rabbitTemplate.convertAndSend(brand.getFanoutExchangeName(), "", brand.toJson());
            brand.upsertField("last_scan_date", DatalakeStorageItem.fromDate(new Date()));
          } else {
            LogManager.getLogger(BrandService.class).info(String.format("BrandService : Too early for %s, next scan planned for %s", brand.getName(), DatalakeStorageItem.fromDate(calendar.getTime())));
          }
        } else {
          LogManager.getLogger(BrandService.class).info(String.format("BrandService : %s will never be reinjected due to its service level", brand.getName()));
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
  }

  /**
   * Get all brands.
   *
   * @return All brands.
   */
  public List<Brand> findAll() {
    LogManager.getLogger(BrandService.class).info("BrandService : Get all brands");
    List<Brand> brands = null;
    try {
      brands = (new Brand()).findAll();
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
    return brands;
  }

  /**
   * Find brand.
   *
   * @param brand Brand to find.
   * @return Brand found.
   */
  public Brand find(Brand brand) {
    LogManager.getLogger(BrandService.class).info(String.format("BrandService : Search brand %s", (brand != null) ? brand.getName() : "null"));
    Brand find = null;
    try {
      find = brand.find();
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
    return find;
  }

  /**
   * Find all brands with given service level.
   *
   * @param serviceLevel Service level to filter brands.
   * @return Brands with given service level.
   */
  public List<Brand> getBrandsByServiceLevel(ServiceLevel serviceLevel) {
    LogManager.getLogger(BrandService.class).info(String.format("BrandService : Get brands with service level %s", serviceLevel.name()));
    List<Brand> brands = null;
    try {
      brands = (new Brand()).search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", serviceLevel.getValue())));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
    return brands;
  }

  /**
   * Create Brand.
   *
   * @param brand Brand to create.
   * @return True if the creation succeeded.
   */
  public boolean create(Brand brand) {
    LogManager.getLogger(BrandService.class).info(String.format("BrandService : Create brand %s", (brand != null) ? brand.toJson() : "null"));
    boolean create = false;
    try {
      create = brand.create();
      if (create && brand.getLastScanDate() == null) {
        brand.setLastScanDate(new Date());
        create = brand.upsert();
        if (create) {
          rabbitTemplate.convertAndSend(brand.getFanoutExchangeName(), "", brand.toJson());
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
    return create;
  }

  /**
   * Update Brand.
   *
   * @param brand Brand to update.
   * @return True if the update succeeded.
   */
  public boolean update(Brand brand) {
    LogManager.getLogger(BrandService.class).info(String.format("BrandService : Update brand %s", (brand != null) ? brand.toJson() : "null"));
    boolean upsert = false;
    try {
      upsert = (brand.find() != null && brand.upsert());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
    return upsert;
  }

  /**
   * Delete a brand.
   *
   * @param brand Brand to delete.
   * @return True if the entity is well deleted.
   */
  public boolean delete(Brand brand) {
    LogManager.getLogger(BrandService.class).info(String.format("BrandService : Delete brand %s", (brand != null) ? brand.getName() : "null"));
    boolean success = false;
    try {
      brand = brand.find();
      success = (brand != null) && (brand.delete());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
    return success;
  }

  /**
   * Find scan period for a given service level.
   *
   * @param serviceLevel Service level to retrieve period of.
   * @return Periodicity (in days) of scan.
   */
  public int getServiceLevelPeriod(ServiceLevel serviceLevel) {
    LogManager.getLogger(BrandService.class).info(String.format("BrandService : Get scan period for service level %s", serviceLevel.name()));
    int period = 0;
    switch (serviceLevel) {
      case GOLD:
        period = scanConfig.getBrandsGoldScanPeriod();
        break;
      case SILVER:
        period = scanConfig.getBrandsSilverScanPeriod();
        break;
      case BRONZE:
        period = scanConfig.getBrandsBronzeScanPeriod();
        break;
      case NONE:
      default:
        period = -1;
        break;
    }
    return period;
  }

  /**
   * Ventilate brands last scan dates.
   *
   * @return True if ventilation succeeded.
   */
  public boolean ventilate() {
    boolean success = ventilate(ServiceLevel.GOLD) && ventilate(ServiceLevel.SILVER) && ventilate(ServiceLevel.BRONZE);
    return success;
  }


  /**
   * Ventilate brands last scan dates for given service level.
   *
   * @return True if ventilation succeeded for given service level.
   */
  private boolean ventilate(ServiceLevel serviceLevel) {
    LogManager.getLogger(BrandService.class).info(String.format("BrandService : Ventilate brands with service level %s", serviceLevel.name()));
    boolean success = true;
    Calendar calendar = Calendar.getInstance();
    List<Brand> brands = getBrandsByServiceLevel(serviceLevel);
    int nbBrands = brands.size();

    if (nbBrands > 0) {
      int period = getServiceLevelPeriod(serviceLevel);
      int periodInMinutes = period * 24 * 60;
      int interval = periodInMinutes / nbBrands;

      for (Brand brand : brands) {
        calendar.add(Calendar.MINUTE, interval * -1);
        brand.setLastScanDate(calendar.getTime());
        success = success && update(brand);
      }
    } else {
      LogManager.getLogger(BrandService.class).info(String.format("BrandService : No brand found for service level %s", serviceLevel.name()));
    }

    return success;
  }


}


