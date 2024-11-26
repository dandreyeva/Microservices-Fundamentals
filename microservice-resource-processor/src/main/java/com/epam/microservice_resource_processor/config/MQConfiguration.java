package com.epam.microservice_resource_processor.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfiguration {

    @Bean
    public Queue messageQueue() {
        return new Queue("resourceIdQueue", false);
    }

    @Bean
    Exchange exchange() {
        return new TopicExchange("exchange", false, false);
    }

    @Bean
    Binding dataBinding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("resourceId");
    }
}
