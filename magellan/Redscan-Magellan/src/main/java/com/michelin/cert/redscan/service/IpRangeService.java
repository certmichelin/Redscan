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
import com.michelin.cert.redscan.utils.models.IpRange;
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
 * IpRange service.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */

@Service
public class IpRangeService {

  @Autowired
  private ScanConfig scanConfig;

  private final RabbitTemplate rabbitTemplate;

  /**
   * Default constructor
   *
   * @param rabbitTemplate Rabbit template.
   */
  public IpRangeService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * Check IPRange to reinject each hour.
   */
  @Scheduled(cron = "${magellan.ipranges.cron}")
  public void scan() {
    LogManager.getLogger(IpRangeService.class).info("IpRangeService : Begin Cron task");
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    try {
      List<IpRange> ipRanges = (new IpRange()).findAll();
      for (IpRange ipRange : ipRanges) {
        if (ipRange.getLastScanDate() != null) {
          calendar.setTime(ipRange.getLastScanDate());
        } else {
          calendar.setTime(new Date(0));
        }

        //Apply service level.
        int period = getServiceLevelPeriod(ServiceLevel.findByValue(ipRange.getServiceLevel()));

        if (period != -1) {
          calendar.add(Calendar.DATE, period);
          if (calendar.getTime().compareTo(now) < 0) {
            LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Reinject %s in the matrix", ipRange.getCidr()));
            rabbitTemplate.convertAndSend(ipRange.getFanoutExchangeName(), "", ipRange.toJson());
            ipRange.upsertField("last_scan_date", DatalakeStorageItem.fromDate(new Date()));
          } else {
            LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Too early for %s, next scan planned for %s", ipRange.getCidr(), DatalakeStorageItem.fromDate(calendar.getTime())));
          }
        } else {
          LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : %s will never be reinjected due to its service level", ipRange.getCidr()));
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(IpRangeService.class).error(String.format("IpRangeService : Datalake storage exception %s", ex.getMessage()));
    }
  }

  /**
   * Get all ipRanges.
   *
   * @return All ipRanges.
   */
  public List<IpRange> findAll() {
    LogManager.getLogger(IpRangeService.class).info("IpRangeService : Get all ipRanges");
    List<IpRange> ipRanges = null;
    try {
      ipRanges = (new IpRange()).findAll();
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(IpRangeService.class).error(String.format("IpRangeService : Datalake storage exception %s", ex.getMessage()));
    }
    return ipRanges;
  }

  /**
   * Find ipRange.
   *
   * @param ipRange IpRange to find.
   * @return IpRange found.
   */
  public IpRange find(IpRange ipRange) {
    LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Search ipRange %s", (ipRange != null) ? ipRange.getCidr() : "null"));
    IpRange find = null;
    try {
      find = ipRange.find();
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(IpRangeService.class).error(String.format("IpRangeService : Datalake storage exception %s", ex.getMessage()));
    }
    return find;
  }

  /**
   * Find all IPRanges with given service level.
   *
   * @param serviceLevel Service level to filter IPRanges.
   * @return IPRanges with given service level.
   */
  public List<IpRange> getIpRangeByServiceLevel(ServiceLevel serviceLevel) {
    LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Get IPRanges with service level %s", serviceLevel.name()));
    List<IpRange> ipRanges = null;
    try {
      ipRanges = (new IpRange()).search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", serviceLevel.getValue())));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(IpRangeService.class).error(String.format("IpRangeService : Datalake storage exception %s", ex.getMessage()));
    }
    return ipRanges;
  }

  /**
   * Create IpRange.
   *
   * @param ipRange IpRange to create.
   * @return True if the creation succeeded.
   */
  public boolean create(IpRange ipRange) {
    LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Create ipRange %s", (ipRange != null) ? ipRange.toJson() : "null"));
    boolean create = false;
    try {
      create = ipRange.create();
      if (create && ipRange.getLastScanDate() == null) {
        ipRange.setLastScanDate(new Date());
        create = ipRange.upsert();
        if (create) {
          rabbitTemplate.convertAndSend(ipRange.getFanoutExchangeName(), "", ipRange.toJson());
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(IpRangeService.class).error(String.format("IpRangeService : Datalake storage exception %s", ex.getMessage()));
    }
    return create;
  }

  /**
   * Update IpRange.
   *
   * @param ipRange IpRange to update.
   * @return True if the update succeeded.
   */
  public boolean update(IpRange ipRange) {
    LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Update ipRange %s", (ipRange != null) ? ipRange.toJson() : "null"));
    boolean upsert = false;
    try {
      upsert = (ipRange.find() != null && ipRange.upsert());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(IpRangeService.class).error(String.format("IpRangeService : Datalake storage exception %s", ex.getMessage()));
    }
    return upsert;
  }

  /**
   * Delete an ipRange.
   *
   * @param ipRange IpRange to delete.
   * @return True if the entity is well deleted.
   */
  public boolean delete(IpRange ipRange) {
    LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Delete ipRange %s", (ipRange != null) ? ipRange.getCidr() : "null"));
    boolean success = false;
    try {
      ipRange = ipRange.find();
      success = (ipRange != null) && (ipRange.delete());
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(IpRangeService.class).error(String.format("IpRangeService : Datalake storage exception %s", ex.getMessage()));
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
    LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Get scan period for service level %s", serviceLevel.name()));
    int period = 0;
    switch (serviceLevel) {
      case GOLD:
        period = scanConfig.getIprangesGoldScanPeriod();
        break;
      case SILVER:
        period = scanConfig.getIprangesSilverScanPeriod();
        break;
      case BRONZE:
        period = scanConfig.getIprangesBronzeScanPeriod();
        break;
      case NONE:
      default:
        period = -1;
        break;
    }
    return period;
  }

  /**
   * Ventilate IPRanges last scan dates.
   *
   * @return True if ventilation succeeded.
   */
  public boolean ventilate() {
    boolean success = ventilate(ServiceLevel.GOLD) && ventilate(ServiceLevel.SILVER) && ventilate(ServiceLevel.BRONZE);
    return success;
  }

  /**
   * Ventilate IPRanges last scan dates for given service level.
   *
   * @return True if ventilation succeeded for given service level.
   */
  private boolean ventilate(ServiceLevel serviceLevel) {
    LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : Ventilate IPRanges with service level %s", serviceLevel.name()));
    boolean success = true;
    Calendar calendar = Calendar.getInstance();
    List<IpRange> ipRanges = getIpRangeByServiceLevel(serviceLevel);
    int nbIpRanges = ipRanges.size();

    if (nbIpRanges > 0) {
      int period = getServiceLevelPeriod(serviceLevel);
      int periodInMinutes = period * 24 * 60;
      int interval = periodInMinutes / nbIpRanges;

      for (IpRange ipRange : ipRanges) {
        calendar.add(Calendar.MINUTE, interval * -1);
        ipRange.setLastScanDate(calendar.getTime());
        success = success && update(ipRange);
      }
    } else {
      LogManager.getLogger(IpRangeService.class).info(String.format("IpRangeService : No IPRange found for service level %s", serviceLevel.name()));
    }

    return success;
  }

}


