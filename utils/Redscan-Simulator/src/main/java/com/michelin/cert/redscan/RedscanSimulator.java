/*
 * Copyright 2024 Michelin CERT (https://cert.michelin.com/)
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

package com.michelin.cert.redscan;

import com.michelin.cert.redscan.utils.datalake.DatalakeStorage;
import com.michelin.cert.redscan.utils.models.Brand;
import com.michelin.cert.redscan.utils.models.IpRange;
import com.michelin.cert.redscan.utils.models.MasterDomain;
import com.michelin.cert.redscan.utils.models.reports.Alert;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import com.michelin.cert.redscan.utils.models.services.HttpService;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class RedscanSimulator implements ApplicationRunner {

  @Value("${datalake.elastic.url}")
  private String elasticSearchUrl;

  @Value("${datalake.elastic.username}")
  private String elasticSearchUsername;

  @Value("${datalake.elastic.password}")
  private String elasticSearchPassword;

  private final RabbitTemplate rabbitTemplate;

  /**
   * Default constructor.
   *
   * @param rabbitTemplate Rabbit template.
   */
  public RedscanSimulator(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * Redscan Simulator main method.
   *
   * @param args App argument.
   * @throws Exception Exception during the run.
   */
  public static void main(String[] args) throws Exception {
    SpringApplication.run(RedscanSimulator.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    System.out.println("Init the datalake...");
    DatalakeStorage.init(elasticSearchUrl, elasticSearchUsername, elasticSearchPassword);

    //Simulating brand.
    if (args.containsOption("brand")) {
      List<String> values = args.getOptionValues("brand");
      if (values != null && !values.isEmpty()) {
        Brand brand = new Brand(values.get(0));
        System.out.println(String.format("Simulating %s brand", brand.getName()));
        if (brand.create()) {
          System.out.println("Brand creation was successful.");
        } else {
          System.out.println("Brand creation failed.");
        }
        rabbitTemplate.convertAndSend(brand.getFanoutExchangeName(), "", brand.toJson());
        System.out.println("Brand was injected");
      }
    }

    //Simulating IP range.
    if (args.containsOption("iprange")) {
      List<String> values = args.getOptionValues("iprange");
      if (values != null && !values.isEmpty()) {
        IpRange iprange = new IpRange(values.get(0));
        System.out.println(String.format("Simulating %s ip range", iprange.getCidr()));
        if (iprange.create()) {
          System.out.println("Ip Range creation was successful.");
        } else {
          System.out.println("Ip Range creation failed.");
        }
        rabbitTemplate.convertAndSend(iprange.getFanoutExchangeName(), "", iprange.toJson());
        System.out.println("Ip Range was injected");
      }
    }

    //Simulating Master domain.
    if (args.containsOption("masterdomain")) {
      List<String> values = args.getOptionValues("masterdomain");
      if (values != null && !values.isEmpty()) {
        MasterDomain masterdomain = new MasterDomain(values.get(0));
        System.out.println(String.format("Simulating %s master domain", masterdomain.getName()));
        if (masterdomain.create()) {
          System.out.println("Master Domain creation was successful.");
        } else {
          System.out.println("Master Domain creation failed.");
        }
        rabbitTemplate.convertAndSend(masterdomain.getFanoutExchangeName(), "", masterdomain.toJson());
        System.out.println("Master Domain was injected");
      }
    }

    //Simulating HTTP service.
    if (args.containsOption("httpservice")) {
      List<String> values = args.getOptionValues("httpservice");
      if (values != null && !values.isEmpty()) {
        HttpService httpservice = new HttpService(values.get(0), "", "443", true);
        System.out.println(String.format("Simulating %s http service", httpservice.toUrl()));
        if (httpservice.create()) {
          System.out.println("Http Service creation was successful.");
        } else {
          System.out.println("Http Service creation failed.");
        }
        rabbitTemplate.convertAndSend(httpservice.getFanoutExchangeName(), "", httpservice.toJson());
        System.out.println("Http Service was injected");
      }
    }

    //Simulating alert.
    if (args.containsOption("alert")) {
      List<String> values = args.getOptionValues("alert");
      if (values != null && !values.isEmpty()) {
        Vulnerability vulnerability = new Vulnerability("id", Integer.parseInt(values.get(0)), "Sample vulnerability", "Sample description", "http://github.com", "origin", new String[]{"tag1", "tag2"});
        Alert alert = new Alert(vulnerability);
        System.out.println(String.format("Simulating %s alert", alert.getSummary()));
        rabbitTemplate.convertAndSend(alert.getFanoutExchangeName(), "", alert.toJson());
        System.out.println("Alert was injected");
      }
    }
  }
}
