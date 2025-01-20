package com.epam.microservice_resource_processor.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private RabbitTemplate rabbitTemplate;

    public MessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * push message to the queue
     * @param queueName name of target queue
     * @param id message to be sent
     * @return true if successful, otherwise false
     */
    public boolean sendQueueMessage(String queueName, String id) {
        try {
            rabbitTemplate.convertAndSend(queueName, id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
