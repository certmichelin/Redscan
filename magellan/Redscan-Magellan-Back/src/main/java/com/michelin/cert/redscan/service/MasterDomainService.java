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
import com.michelin.cert.redscan.utils.models.MasterDomain;
import com.michelin.cert.redscan.utils.models.ServiceLevel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kong.unirest.json.JSONObject;

import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;



/**
 * Master domain service.
 *
 * @author Axel REMACK
 * @author Maxime ESCOURBIAC
 */
@Service
public class MasterDomainService extends DatalakeStorageItemService<MasterDomain> {

  @Autowired
  private ScanConfig scanConfig;

  /**
   * Default constructor.
   *
   * @param scanConfig Scan configuration.
   */
  @Autowired
  public MasterDomainService(ScanConfig scanConfig) {
    super(new MasterDomain());
    this.scanConfig = scanConfig;
  }

  /**
   * Find all master domains with given service level.
   *
   * @param serviceLevel Service level to filter master domains.
   * @return Master domains with given service level.
   */
  public List<MasterDomain> getMasterDomainsByServiceLevel(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get %s with service level %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    List<MasterDomain> masterDomains = null;
    try {
      masterDomains = item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", serviceLevel.getValue())));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return masterDomains;
  }

  /**
   * Find in Scope master domains with given service level.
   *
   * @param serviceLevel Service level to filter master domains.
   * @return In scope master domains with given service level.
   */
  public List<MasterDomain> getInScopeMasterDomainsByServiceLevel(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get InScope %s with service level %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    List<MasterDomain> masterDomains = null;
    try {
      masterDomains = item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"inScope\" :\"true\" }}, { \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", serviceLevel.getValue())));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return masterDomains;
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
   * Create Master domain.
   *
   * @param masterDomain Master domain to create.
   * @return True if the creation succeeded.
   */
  @Override
  public boolean create(MasterDomain masterDomain) {
    boolean createMasterDomain = false;
    try {
      if (super.create(masterDomain) && masterDomain.isInScope() && masterDomain.getLastScanDate() == null) {
        masterDomain.setLastScanDate(new Date());
        if (masterDomain.upsert()) {
          reinject(masterDomain.getId());
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return createMasterDomain;
  }

  /**
   * Ventilate master domains' last scan dates.
   *
   * @return True if ventilation succeeded.
   */
  public boolean ventilate() {
    boolean success = ventilate(ServiceLevel.GOLD) && ventilate(ServiceLevel.SILVER) && ventilate(ServiceLevel.BRONZE);
    return success;
  }

  /**
   * Ventilate master domains' last scan dates for given service level.
   *
   * @return True if ventilation succeeded for given service level.
   */
  private boolean ventilate(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Ventilate %s with service level %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    boolean success = true;
    Calendar calendar = Calendar.getInstance();
    List<MasterDomain> masterDomains = getInScopeMasterDomainsByServiceLevel(serviceLevel);
    int nbItems = masterDomains.size();

    if (nbItems > 0) {
      int period = getServiceLevelPeriod(serviceLevel);
      int periodInMinutes = period * 24 * 60;
      int interval = periodInMinutes / nbItems;

      for (MasterDomain masterDomain : masterDomains) {
        calendar.add(Calendar.MINUTE, interval * -1);
        masterDomain.setLastScanDate(calendar.getTime());
        success = success && update(masterDomain);
      }
    } else {
      LogManager.getLogger(getClass()).info(String.format("%s : No %s found for service level %s", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    }

    return success;
  }

  /**
   * Reinject necessary master domains.
   *
   * @return True if reinjection succeeded.
   */
  @Scheduled(cron = "${magellan.masterdomains.cron}")
  public boolean reinjectAll() {
    LogManager.getLogger(getClass()).info(String.format("%s : Begin reinjection of %ss.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    boolean success = true;

    try {
      List<MasterDomain> masterDomains = findInScope();
      for (MasterDomain masterDomain : masterDomains) {
        if (masterDomain.getLastScanDate() != null) {
          calendar.setTime(masterDomain.getLastScanDate());
        } else {
          calendar.setTime(new Date(0));
        }

        int period = getServiceLevelPeriod(ServiceLevel.findByValue(masterDomain.getServiceLevel()));

        if (period != -1) {
          calendar.add(Calendar.DATE, period);
          if (calendar.getTime().compareTo(now) < 0) {
            success = success && reinject(masterDomain.getId()) && masterDomain.upsertField("last_scan_date", DatalakeStorageItem.fromDate(new Date()));
          } else {
            LogManager.getLogger(getClass()).info(String.format("%s : Too early for %s, next scan planned for %s.", getClass().getSimpleName(), masterDomain.getName(), DatalakeStorageItem.fromDate(calendar.getTime())));
          }
        } else {
          LogManager.getLogger(getClass()).info(String.format("%s : %s will never be reinjected due to its service level.", getClass().getSimpleName(), masterDomain.getName()));
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return success;
  }

  /**
   * Get all master domains that are inside the scope.
   *
   * @return All in scope master domains.
   */
  public List<MasterDomain> findInScope() {
    LogManager.getLogger(getClass()).info(String.format("%s : Get in scope %ss.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    List<MasterDomain> itemsInScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:true)\"}}");
      itemsInScope = item.search(query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsInScope;
  }

  /**
   * Get all master domains that are inside the scope with particular sorting.
   *
   * @param sort Elastic sort query.
   * @return All in scope master domains, sorted.
   */
  public List<MasterDomain> findInScope(String sort) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get in scope %ss with sorting %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), sort));
    List<MasterDomain> itemsInScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:true)\"}}");
      itemsInScope = item.search(query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsInScope;
  }

  /**
   * Get all master domains that are inside the scope with pagination.
   *
   * @param page Page number.
   * @param size Number of master domains in each page.
   * @return All in scope master domains with pagination.
   */
  public List<MasterDomain> findInScope(String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get in scope %ss with pagination.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    List<MasterDomain> itemsInScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:true)\"}}");
      itemsInScope = item.search(page, size, query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsInScope;
  }

  /**
   * Get all master domains that are inside the scope with pagination and particular sorting.
   *
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of master domains in each page.
   * @return All in scope master domains with pagination.
   */
  public List<MasterDomain> findInScope(String sort, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get in scope %ss with pagination and sorting %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), sort));
    List<MasterDomain> itemsInScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:true)\"}}");
      itemsInScope = item.search(page, size, query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsInScope;
  }

  /**
   * Get all master domains that are outside the scope.
   *
   * @return All out of scope master domains.
   */
  public List<MasterDomain> findOutOfScope() {
    LogManager.getLogger(getClass()).info(String.format("%s : Get out of scope %ss.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    List<MasterDomain> itemsOutOfScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:false)\"}}");
      itemsOutOfScope = item.search(query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsOutOfScope;
  }

  /**
   * Get all master domains that are outside the scope with particular sorting.
   *
   * @return All out of scope master domains, sorted.
   */
  public List<MasterDomain> findOutOfScope(String sort) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get out of scope %ss with sorting %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), sort));
    List<MasterDomain> itemsOutOfScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:false)\"}}");
      itemsOutOfScope = item.search(query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsOutOfScope;
  }

  /**
   * Get all master domains that are outside the scope with pagination.
   *
   * @param page Page number.
   * @param size Number of master domains in each page.
   * @return All out of scope master domains with pagination.
   */
  public List<MasterDomain> findOutOfScope(String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get out of scope %ss with pagination.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    List<MasterDomain> itemsOutOfScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:false)\"}}");
      itemsOutOfScope = item.search(page, size, query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsOutOfScope;
  }

  /**
   * Get all master domains that are outside the scope with pagination and particular sorting.
   *
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of master domains in each page.
   * @return All out of scope master domains with pagination.
   */
  public List<MasterDomain> findOutOfScope(String sort, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get out of scope %ss with pagination and sorting %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), sort));
    List<MasterDomain> itemsOutOfScope = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(inScope:false)\"}}");
      itemsOutOfScope = item.search(page, size, query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsOutOfScope;
  }

  /**
   * Get all master domains that have not been reviewed yet.
   *
   * @return All master domains to review.
   */
  public List<MasterDomain> findToReview() {
    LogManager.getLogger(getClass()).info(String.format("%s : Get %ss to review.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    List<MasterDomain> itemsToReview = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(reviewed:false)\"}}");
      itemsToReview = item.search(query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsToReview;
  }

  /**
   * Get all master domains that have not been reviewed yet with particular sorting.
   *
   * @param sort Elastic sort query.
   * @return All master domains to review, sorted.
   */
  public List<MasterDomain> findToReview(String sort) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get %ss to review with sorting %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), sort));
    List<MasterDomain> itemsToReview = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(reviewed:false)\"}}");
      itemsToReview = item.search(query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsToReview;
  }

  /**
   * Get all master domains that have not been reviewed yet, with pagination.
   *
   * @param page Page number.
   * @param size Number of master domains in each page.
   * @return All master domains to review, with pagination.
   */
  public List<MasterDomain> findToReview(String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get %ss to review with pagination.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    List<MasterDomain> itemsToReview = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(reviewed:false)\"}}");
      itemsToReview = item.search(page, size, query);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsToReview;
  }

  /**
   * Get all master domains that have not been reviewed yet, with pagination and particular sorting.
   *
   * @param sort Elastic sort query.
   * @param page Page number.
   * @param size Number of master domains in each page.
   * @return All master domains to review, with pagination.
   */
  public List<MasterDomain> findToReview(String sort, String page, String size) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get %ss to review with pagination and sorting %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), sort));
    List<MasterDomain> itemsToReview = new ArrayList<>();
    try {
      JSONObject query = new JSONObject("{\"query_string\" : {\"query\":\"(reviewed:false)\"}}");
      itemsToReview = item.search(page, size, query, sort);
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return itemsToReview;
  }
}
