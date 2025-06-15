package com.karboncard.assignment.notificationservice.worker.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karboncard.assignment.notificationservice.model.dlq.DeadLetterPayload;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterQueueConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "${app.kafka.dead-letter-topic:notification-dlq}",
            groupId = "${spring.kafka.consumer.group-id:notification-dlq-consumer-group}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            DeadLetterPayload payload = objectMapper.readValue(record.value(), DeadLetterPayload.class);
            logger.error("Processing DLQ message: {}", payload);
            // TODO: Optionally persist DLQ payload in DB for audit or alerting
        } catch (Exception e) {
            logger.error("Failed to process DLQ message: {}", record.value(), e);
        }
    }
}