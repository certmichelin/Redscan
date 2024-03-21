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

import com.michelin.cert.redscan.utils.models.MasterDomain;

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
 * @author Sylvain VAISSIER
 * @author Maxence SCHMITT
 */
@Configuration
public class RabbitMqConfig {
  
  ///**
  // * QUEUE_DOMAINS.
  // */
  public static final String QUEUE_MASTERDOMAINS = "com.michelin.cert.urlcrazy.masterdomains";
    
  /**
    * Queue configuration method.
    *
    * @return Declarables.
    */
  @Bean
  public Declarables fanoutBindings() {
    Queue queue = new Queue(QUEUE_MASTERDOMAINS, false);
    FanoutExchange fanoutExchange = new FanoutExchange(new MasterDomain().getFanoutExchangeName(), true, false);
    return new Declarables(queue, fanoutExchange, BindingBuilder.bind(queue).to(fanoutExchange));
  }
}
