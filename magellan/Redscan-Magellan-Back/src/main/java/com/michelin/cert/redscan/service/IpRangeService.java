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
import com.michelin.cert.redscan.utils.models.IpRange;
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
 * IP range service.
 *
 * @author Axel REMACK
 * @author Maxime ESCOURBIAC
 */
@Service
public class IpRangeService extends DatalakeStorageItemService<IpRange> {

  @Autowired
  private ScanConfig scanConfig;

  /**
   * Default constructor.
   */
  @Autowired
  public IpRangeService(ScanConfig scanConfig) {
    super(new IpRange());
    this.scanConfig = scanConfig;
  }

  /**
   * Find all ip ranges with given service level.
   *
   * @param serviceLevel Service level to filter ip ranges.
   * @return Ip ranges with given service level.
   */
  public List<IpRange> getIpRangesByServiceLevel(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Get %s with service level %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    List<IpRange> ipRanges = null;
    try {
      ipRanges = item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", serviceLevel.getValue())));
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }
    return ipRanges;
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
   * Ventilate ip ranges' last scan dates.
   *
   * @return True if ventilation succeeded.
   */
  public boolean ventilate() {
    boolean success = ventilate(ServiceLevel.GOLD) && ventilate(ServiceLevel.SILVER) && ventilate(ServiceLevel.BRONZE);
    return success;
  }

  /**
   * Ventilate ip ranges' last scan dates for given service level.
   *
   * @return True if ventilation succeeded for given service level.
   */
  private boolean ventilate(ServiceLevel serviceLevel) {
    LogManager.getLogger(getClass()).info(String.format("%s : Ventilate %s with service level %s.", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    boolean success = true;
    Calendar calendar = Calendar.getInstance();
    List<IpRange> ipRanges = getIpRangesByServiceLevel(serviceLevel);
    int nbItems = ipRanges.size();

    if (nbItems > 0) {
      int period = getServiceLevelPeriod(serviceLevel);
      int periodInMinutes = period * 24 * 60;
      int interval = periodInMinutes / nbItems;

      for (IpRange ipRange : ipRanges) {
        calendar.add(Calendar.MINUTE, interval * -1);
        ipRange.setLastScanDate(calendar.getTime());
        success = success && update(ipRange);
      }
    } else {
      LogManager.getLogger(getClass()).info(String.format("%s : No %s found for service level %s", getClass().getSimpleName(), this.item.getClass().getSimpleName(), serviceLevel.name()));
    }

    return success;
  }

  /**
   * Reinject necessary ip ranges.
   *
   * @return True if reinjection succeeded.
   */
  @Scheduled(cron = "${magellan.ipranges.cron}")
  public boolean reinjectAll() {
    LogManager.getLogger(getClass()).info(String.format("%s : Begin reinjection of %ss.", getClass().getSimpleName(), this.item.getClass().getSimpleName()));
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    boolean success = true;

    try {
      List<IpRange> ipRanges = item.findAll();
      for (IpRange ipRange : ipRanges) {
        if (ipRange.getLastScanDate() != null) {
          calendar.setTime(ipRange.getLastScanDate());
        } else {
          calendar.setTime(new Date(0));
        }

        int period = getServiceLevelPeriod(ServiceLevel.findByValue(ipRange.getServiceLevel()));

        if (period != -1) {
          calendar.add(Calendar.DATE, period);
          if (calendar.getTime().compareTo(now) < 0) {
            success = success && reinject(ipRange.getId()) && ipRange.upsertField("last_scan_date", DatalakeStorageItem.fromDate(new Date()));
          } else {
            LogManager.getLogger(getClass()).info(String.format("%s : Too early for %s, next scan planned for %s.", getClass().getSimpleName(), ipRange.getCidr(), DatalakeStorageItem.fromDate(calendar.getTime())));
          }
        } else {
          LogManager.getLogger(getClass()).info(String.format("%s : %s will never be reinjected due to its service level.", getClass().getSimpleName(), ipRange.getCidr()));
        }
      }
    } catch (DatalakeStorageException ex) {
      LogManager.getLogger(getClass()).error(String.format("%s : Datalake storage exception %s", getClass().getSimpleName(), ex.getMessage()));
    }

    return success;
  }

}
