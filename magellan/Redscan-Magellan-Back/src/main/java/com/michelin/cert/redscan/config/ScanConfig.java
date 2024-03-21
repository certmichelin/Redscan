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

package com.michelin.cert.redscan.config;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configure ElasticSearch datalake.
 *
 * @author Maxime ESCOURBIAC
 */
@Configuration
public class ScanConfig {

  @Value("${magellan.brands.gold.scan.period}")
  private int brandsGoldScanPeriod;

  @Value("${magellan.brands.silver.scan.period}")
  private int brandsSilverScanPeriod;

  @Value("${magellan.brands.bronze.scan.period}")
  private int brandsBronzeScanPeriod;

  @Value("${magellan.ipranges.gold.scan.period}")
  private int iprangesGoldScanPeriod;

  @Value("${magellan.ipranges.silver.scan.period}")
  private int iprangesSilverScanPeriod;

  @Value("${magellan.ipranges.bronze.scan.period}")
  private int iprangesBronzeScanPeriod;

  @Value("${magellan.masterdomains.gold.scan.period}")
  private int masterdomainsGoldScanPeriod;

  @Value("${magellan.masterdomains.silver.scan.period}")
  private int masterdomainsSilverScanPeriod;

  @Value("${magellan.masterdomains.bronze.scan.period}")
  private int masterdomainsBronzeScanPeriod;

  @Value("${magellan.brands.cron}")
  private String brandCron;

  @Value("${magellan.ipranges.cron}")
  private String iprangesCron;

  @Value("${magellan.masterdomains.cron}")
  private String masterDomainCron;

  /**
   * Default constructor.
   */
  @Autowired
  public ScanConfig() {
  }

  /**
   * Log cron values.
   */
  @PostConstruct
  public void logCron() {
    //Log info from cron value.
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.brands.gold.scan.period : %d day(s)", brandsGoldScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.brands.silver.scan.period : %d day(s)", brandsSilverScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.brands.bronze.scan.period : %d day(s)", brandsBronzeScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.ipranges.gold.scan.period : %d day(s)", iprangesGoldScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.ipranges.silver.scan.period : %d day(s)", iprangesSilverScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.ipranges.bronze.scan.period : %d day(s)", iprangesBronzeScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.masterdomains.gold.scan.period : %d day(s)", masterdomainsGoldScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.masterdomains.silver.scan.period : %d day(s)", masterdomainsSilverScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.masterdomains.bronze.scan.period : %d day(s)", masterdomainsBronzeScanPeriod));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.brands.cron value : (%s)", brandCron));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.ipranges.cron value : (%s)", iprangesCron));
    LogManager.getLogger(ScanConfig.class).info(String.format("magellan.masterdomains.cron value : (%s)", masterDomainCron));
  }

  /**
   * Brand gold scan period.
   *
   * @return Brand gold scan period.
   */
  public int getBrandsGoldScanPeriod() {
    return brandsGoldScanPeriod;
  }

  /**
   * Brand silver scan period.
   *
   * @return Brand silver scan period.
   */
  public int getBrandsSilverScanPeriod() {
    return brandsSilverScanPeriod;
  }

  /**
   * Brand bronze scan period.
   *
   * @return Brand bronze scan period.
   */
  public int getBrandsBronzeScanPeriod() {
    return brandsBronzeScanPeriod;
  }

  /**
   * Ip range gold scan period.
   *
   * @return Ip range gold scan period.
   */
  public int getIprangesGoldScanPeriod() {
    return iprangesGoldScanPeriod;
  }

  /**
   * Ip range silver scan period.
   *
   * @return Ip range silver scan period.
   */
  public int getIprangesSilverScanPeriod() {
    return iprangesSilverScanPeriod;
  }

  /**
   * Ip range bronze scan period.
   *
   * @return Ip range bronze scan period.
   */
  public int getIprangesBronzeScanPeriod() {
    return iprangesBronzeScanPeriod;
  }

  /**
   * Master domain gold scan period.
   *
   * @return Master domain gold scan period.
   */
  public int getMasterdomainsGoldScanPeriod() {
    return masterdomainsGoldScanPeriod;
  }

  /**
   * Master domain silver scan period.
   *
   * @return Master domain silver scan period.
   */
  public int getMasterdomainsSilverScanPeriod() {
    return masterdomainsSilverScanPeriod;
  }

  /**
   * Master domain bronze scan period.
   *
   * @return Master domain bronze scan period.
   */
  public int getMasterdomainsBronzeScanPeriod() {
    return masterdomainsBronzeScanPeriod;
  }

}