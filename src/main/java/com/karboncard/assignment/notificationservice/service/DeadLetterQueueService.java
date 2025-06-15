package com.karboncard.assignment.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karboncard.assignment.notificationservice.model.dlq.DeadLetterPayload;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeadLetterQueueService {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.dead-letter-topic:notification-dlq}")
    private String deadLetterTopic;

    public void sendToDLQ(DeadLetterPayload payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(deadLetterTopic, payload.getOriginalKey(), payloadJson);
            logger.warn("Sent message to DLQ: {}", payloadJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize DLQ payload", e);
        } catch (Exception ex) {
            logger.error("Failed to send message to DLQ", ex);
        }
    }
}