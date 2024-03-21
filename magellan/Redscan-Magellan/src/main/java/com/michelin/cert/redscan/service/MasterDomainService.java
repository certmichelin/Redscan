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
import com.michelin.cert.redscan.utils.models.MasterDomain;
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
 * MasterDomain service.
 *
 * @author Maxime ESCOURBIAC
 * @author Maxence SCHMITT
 * @author Axel REMACK
 */
@Service
public class MasterDomainService {

  @Autowired
  private ScanConfig scanConfig;

  private final RabbitTemplate rabbitTemplate;

  /**
   * Default constructor
   *
   * @param rabbitTemplate Rabbit template.
   */
  public MasterDomainService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * Check master domains to reinject each hour.
   */
  @Scheduled(cron = "${magellan.masterdomains.cron}")
  public void scan() {
    LogManager.getLogger(MasterDomainService.class).info("MasterDomainService : Begin Cron task");
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();

    try {
      for (MasterDomain masterDomain : getInScopeMasterDomains()) {

        if (masterDomain.getLastScanDate() != null) {
          calendar.setTime(masterDomain.getLastScanDate());
        } else {
          calendar.setTime(new Date(0));
        }

        //Apply service level.
        int period = getServiceLevelPeriod(ServiceLevel.findByValue(masterDomain.getServiceLevel()));

        if (period != -1) {
          calendar.add(Calendar.DATE, period);
          if (calendar.getTime().compareTo(now) < 0) {
            LogManager.getLogger(MasterDomainService.class.getName()).info(String.format("MasterDomainService : Reinject %s in the matrix", masterDomain.getName()));
            rabbitTemplate.convertAndSend(masterDomain.getFanoutExchangeName(), "", masterDomain.toJson());
            masterDomain.upsertField("last_scan_date", DatalakeStorageItem.fromDate(new Date()));
          } else {
            LogManager.getLogger(MasterDomainService.class.getName()).info(String.format("MasterDomainService : Too early for %s, next scan planned for %s", masterDomain.getName(), DatalakeStorageItem.fromDate(calendar.getTime())));
          }
        } else {
          LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : %s will never be reinjected due to its service level", masterDomain.getName()));
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
  }

  /**
   * Get all master domains.
   *
   * @return All master domains.
   */
  public List<MasterDomain> findAll() {
    LogManager.getLogger(MasterDomainService.class).info("MasterDomainService : Get all master domains");
    List<MasterDomain> masterdomains = null;
    try {
      masterdomains = (new MasterDomain()).findAll();
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return masterdomains;
  }

  /**
   * Find master domain.
   *
   * @param masterDomain MasterDomain to find.
   * @return MasterDomain found.
   */
  public MasterDomain find(MasterDomain masterDomain) {
    LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : Search masterDomain %s", (masterDomain != null) ? masterDomain.getName() : "null"));
    MasterDomain find = null;
    try {
      find = masterDomain.find();
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return find;
  }

  /**
   * Find in Scope master domains.
   *
   * @return In scope master domains.
   */
  public List<MasterDomain> getInScopeMasterDomains() {
    LogManager.getLogger(MasterDomainService.class).info("MasterDomainService : Get in scope master domains");
    List<MasterDomain> masterDomains = null;
    try {
      masterDomains = (new MasterDomain()).search(new JSONObject("{ \"bool\" : { \"must\" : [{ \"term\" : { \"inScope\" :\"true\" }}] }}"));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return masterDomains;
  }


  /**
   * Find in Scope master domains with given service level.
   *
   * @param serviceLevel Service level to filter master domains.
   * @return In scope master domains with given service level.
   */
  public List<MasterDomain> getInScopeMasterDomains(ServiceLevel serviceLevel) {
    LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : Get in scope master domains for service level %s", serviceLevel.name()));
    List<MasterDomain> masterDomains = null;
    try {
      masterDomains = (new MasterDomain()).search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"inScope\" :\"true\" }}, { \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", serviceLevel.getValue())));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return masterDomains;
  }

  /**
   * Find out of scope master domains.
   *
   * @return Out of scope master domains.
   */
  public List<MasterDomain> getNotInScopeMasterDomains() {
    LogManager.getLogger(MasterDomainService.class).info("MasterDomainService : Get out of scope master domains");
    List<MasterDomain> masterDomains = null;
    try {
      masterDomains = (new MasterDomain()).search(new JSONObject("{ \"bool\" : { \"must\" : [{ \"term\" : { \"inScope\" :\"false\" }}] }}"));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return masterDomains;
  }

  /**
   * Find master domains to review.
   *
   * @return Master domains to review.
   */
  public List<MasterDomain> getMasterDomainsToReview() {
    LogManager.getLogger(MasterDomainService.class).info("MasterDomainService : Get master domains to review");
    List<MasterDomain> masterDomains = null;
    try {
      masterDomains = (new MasterDomain()).search(new JSONObject("{ \"bool\" : { \"must\" : [{ \"term\" : { \"reviewed\" :\"false\" }}] }}"));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return masterDomains;
  }

  /**
   * Create Master domain.
   *
   * @param masterDomain Master domain to create.
   * @return True if the creation succeeded.
   */
  public boolean create(MasterDomain masterDomain) {
    boolean createMasterDomain = false;
    try {
      if (masterDomain != null) {
        LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : Create masterDomain %s", masterDomain.toJson()));
        if (masterDomain.getLastScanDate() == null) {
          masterDomain.setParent("");
          masterDomain.setReviewed(true);
        }
        createMasterDomain = masterDomain.create();
        if (createMasterDomain && masterDomain.isInScope() && masterDomain.getLastScanDate() == null) {
          masterDomain.setLastScanDate(new Date());
          createMasterDomain = masterDomain.upsert();
          if (createMasterDomain) {
            rabbitTemplate.convertAndSend(masterDomain.getFanoutExchangeName(), "", masterDomain.toJson());
          }
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return createMasterDomain;
  }

  /**
   * Delete a masterDomain.
   *
   * @param masterDomain Master domain to delete.
   * @return True if the entity is well deleted.
   */
  public boolean delete(MasterDomain masterDomain) {
    LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : Delete masterDomain %s", (masterDomain != null) ? masterDomain.getName() : "null"));
    boolean success = false;
    try {
      masterDomain = masterDomain.find();
      success = (masterDomain != null) && (masterDomain.delete());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return success;
  }

  /**
   * Update Master domain.
   *
   * @param masterDomain Master domain to update.
   * @param send Send to queue if the master domain is in scope.
   * @return True if the update succeed.
   */
  public boolean update(MasterDomain masterDomain, boolean send) {
    LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : Upsert masterDomain %s", (masterDomain != null) ? masterDomain.toJson() : "null"));
    boolean result = false;
    try {
      result = masterDomain.upsert();
      if (result && masterDomain.isInScope() == true && send) {
        result = masterDomain.upsertField("last_scan_date", DatalakeStorageItem.fromDate(new Date()));
        rabbitTemplate.convertAndSend(masterDomain.getFanoutExchangeName(), "", masterDomain.toJson());
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(MasterDomainService.class).error(String.format("MasterDomainService : Datalake storage exception %s", ex.getMessage()));
    }
    return result;
  }


  /**
   * Find scan period for a given service level.
   *
   * @param serviceLevel Service level to retrieve period of.
   * @return Periodicity (in days) of scan.
   */
  public int getServiceLevelPeriod(ServiceLevel serviceLevel) {
    LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : Get scan period for service level %s", serviceLevel.name()));
    int period = 0;
    switch (serviceLevel) {
      case GOLD:
        period = scanConfig.getMasterdomainsGoldScanPeriod();
        break;
      case SILVER:
        period = scanConfig.getMasterdomainsSilverScanPeriod();
        break;
      case BRONZE:
        period = scanConfig.getMasterdomainsBronzeScanPeriod();
        break;
      case NONE:
      default:
        period = -1;
        break;
    }
    return period;
  }


  /**
   * Ventilate master domains last scan dates.
   *
   * @return True if ventilation succeeded.
   */
  public boolean ventilate() {
    boolean success = ventilate(ServiceLevel.GOLD) && ventilate(ServiceLevel.SILVER) && ventilate(ServiceLevel.BRONZE);
    return success;
  }


  /**
   * Ventilate master domains last scan dates for given service level.
   *
   * @return True if ventilation succeeded for given service level.
   */
  private boolean ventilate(ServiceLevel serviceLevel) {
    LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : Ventilate master domains with service level %s", serviceLevel.name()));
    boolean success = true;
    Calendar calendar = Calendar.getInstance();
    List<MasterDomain> masterDomains = getInScopeMasterDomains(serviceLevel);
    int nbMasterdomains = masterDomains.size();

    if (nbMasterdomains > 0) {
      int period = getServiceLevelPeriod(serviceLevel);
      int periodInMinutes = period * 24 * 60;
      int interval = periodInMinutes / nbMasterdomains;

      for (MasterDomain masterDomain : masterDomains) {
        calendar.add(Calendar.MINUTE, interval * -1);
        masterDomain.setLastScanDate(calendar.getTime());
        success = success && update(masterDomain, false);
      }
    } else {
      LogManager.getLogger(MasterDomainService.class).info(String.format("MasterDomainService : No master domain in scope for service level %s", serviceLevel.name()));
    }

    return success;
  }


}

