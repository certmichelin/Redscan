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
public class BrandService extends DatalakeStorageItemService<Brand> {

  @Autowired
  private ScanConfig scanConfig;


  /**
   * Default constructor.
   */
  @Autowired
  public BrandService(ScanConfig scanConfig) {
    super(new Brand());
    this.scanConfig = scanConfig;
  }

  /**
   * Find all brands with given service level.
   *
   * @param serviceLevel Service level to filter brands.
   * @return Brands with given service level.
   */
  public List<Brand> getBrandsByServiceLevel(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get %s with service level %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    List<Brand> brands = null;
    try {
      brands = item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", serviceLevel.getValue())));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return brands;
  }

  /**
   * Find scan period for a given service level.
   *
   * @param serviceLevel Service level to retrieve period of.
   * @return Periodicity (in days) of scan.
   */
  public int getServiceLevelPeriod(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get scan period for service level %s.", getClass().getSimpleName(), serviceLevel.name()));
    int period;
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
   * Create Brand.
   *
   * @param brand Brand to create.
   * @return True if the creation succeeded.
   */
  @Override
  public boolean create(Brand brand) {
    boolean create = false;
    try {
      create = super.create(brand);
      if (create && brand.getLastScanDate() == null) {
        brand.setLastScanDate(new Date());
        create = brand.upsert();
        if (create) {
          reinject(brand.getId());
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(BrandService.class).error(String.format("BrandService : Datalake storage exception %s", ex.getMessage()));
    }
    return create;
  }
  
  /**
   * Ventilate brands' last scan dates.
   *
   * @return True if ventilation succeeded.
   */
  public boolean ventilate() {
    boolean success = ventilate(ServiceLevel.GOLD) && ventilate(ServiceLevel.SILVER) && ventilate(ServiceLevel.BRONZE);
    return success;
  }

  /**
   * Ventilate brands' last scan dates for given service level.
   *
   * @return True if ventilation succeeded for given service level.
   */
  private boolean ventilate(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Ventilate %s with service level %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    boolean success = true;
    Calendar calendar = Calendar.getInstance();
    List<Brand> brands = getBrandsByServiceLevel(serviceLevel);
    int nbItems = brands.size();

    if (nbItems > 0) {
      int period = getServiceLevelPeriod(serviceLevel);
      int periodInMinutes = period * 24 * 60;
      int interval = periodInMinutes / nbItems;

      for (Brand brand : brands) {
        calendar.add(Calendar.MINUTE, interval * -1);
        brand.setLastScanDate(calendar.getTime());
        success = success && update(brand);
      }
    } else {
      LogManager.getLogger(getClass()).info(String.format("%s : No %s found for service level %s", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    }

    return success;
  }

  /**
   * Reinject necessary brands.
   *
   * @return True if reinjection succeeded.
   */
  @Scheduled(cron = "${magellan.brands.cron}")
  public boolean reinjectAll() {
    LogManager.getLogger(getClass()).info(String.format("%s : Begin reinjection of %ss.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    boolean success = true;

    try {
      List<Brand> brands = item.findAll();
      for (Brand brand : brands) {
        if (brand.getLastScanDate() != null) {
          calendar.setTime(brand.getLastScanDate());
        } else {
          calendar.setTime(new Date(0));
        }

        int period = getServiceLevelPeriod(ServiceLevel.findByValue(brand.getServiceLevel()));

        if (period != -1) {
          calendar.add(Calendar.DATE, period);
          if (calendar.getTime().compareTo(now) < 0) {
            success = success && reinject(brand.getId()) && brand.upsertField("last_scan_date", DatalakeStorageItem.fromDate(new Date()));
          } else {
            LogManager.getLogger(getClass()).info(String.format("%s : Too early for %s, next scan planned for %s.", getClass().getSimpleName(), brand.getName(), DatalakeStorageItem.fromDate(calendar.getTime())));
          }
        } else {
          LogManager.getLogger(getClass()).info(String.format("%s : %s will never be reinjected due to its service level.", getClass().getSimpleName(), brand.getName()));
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return success;
  }

}
