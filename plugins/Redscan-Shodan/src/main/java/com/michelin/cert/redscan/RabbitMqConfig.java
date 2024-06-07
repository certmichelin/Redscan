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

package com.michelin.cert.redscan;

import com.michelin.cert.redscan.utils.models.Domain;
import com.michelin.cert.redscan.utils.models.Ip;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure Rabbit MQ messages.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */
@Configuration
public class RabbitMqConfig {

  /**
   * QUEUE_DOMAINS.
   */
  public static final String QUEUE_DOMAINS = "com.michelin.cert.shodan.domains";
  
  /**
   * QUEUE_IPS.
   */
  public static final String QUEUE_IPS = "com.michelin.cert.shodan.ips";

  /**
   * Queue configuration method.
   *
   * @return Declarables.
   */
  @Bean
  public Declarables domainsFanoutBindings() {
    Queue queue = new Queue(QUEUE_DOMAINS, true);
    FanoutExchange fanoutExchange = new FanoutExchange((new Domain()).getFanoutExchangeName(), true, false);
    return new Declarables(queue, fanoutExchange, BindingBuilder.bind(queue).to(fanoutExchange));
  }
  
  /**
   * Queue configuration method.
   *
   * @return Declarables.
   */
  @Bean
  public Declarables ipsFanoutBindings() {
    Queue queue = new Queue(QUEUE_IPS, true);
    FanoutExchange fanoutExchange = new FanoutExchange((new Ip().getFanoutExchangeName()), true, false);
    return new Declarables(queue, fanoutExchange, BindingBuilder.bind(queue).to(fanoutExchange));
  }
}
