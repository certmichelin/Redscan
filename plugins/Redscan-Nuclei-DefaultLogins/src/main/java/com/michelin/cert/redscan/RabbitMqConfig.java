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

package com.michelin.cert.redscan;

import com.michelin.cert.redscan.utils.models.services.HttpService;

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
 * @author Axel REMACK
 */
@Configuration
public class RabbitMqConfig {
        
        
  /**
   * QUEUE_HTTP_SERVICE.
   */
  public static final String QUEUE_HTTP_SERVICES = "com.michelin.cert.nucleidefaultlogins.http_services";

  /**
   * Queue configuration method.
   *
   * @return Declarables.
   */
  @Bean
  public Declarables fanoutBindings() {
    Queue queue = new Queue(QUEUE_HTTP_SERVICES, true);
    FanoutExchange fanoutExchange = new FanoutExchange(new HttpService().getFanoutExchangeName(), true, false);
    return new Declarables(queue, fanoutExchange, BindingBuilder.bind(queue).to(fanoutExchange));
  }
}
