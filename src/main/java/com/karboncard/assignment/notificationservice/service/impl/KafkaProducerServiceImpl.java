package com.karboncard.assignment.notificationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karboncard.assignment.notificationservice.config.KafkaConfig;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.service.KafkaProducerService;
import com.karboncard.assignment.notificationservice.util.MetricsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaConfig kafkaConfig;
    private final MetricsUtil metricsUtil;

    @Override
    public boolean sendToEmailTopic(Notification notification) {
        return sendToKafka(notification, kafkaConfig.getEmailTopic());
    }

    @Override
    public boolean sendToSmsTopic(Notification notification) {
        return sendToKafka(notification, kafkaConfig.getSmsTopic());
    }

    @Override
    public boolean sendToPushTopic(Notification notification) {
        return sendToKafka(notification, kafkaConfig.getPushTopic());
    }

    @Override
    public boolean sendWithPriority(Notification notification, String topic) {
        return sendToKafkaWithHeaders(notification, topic,
                Map.of("priority", notification.getPriority().toString()));
    }

    private boolean sendToKafka(Notification notification, String topic) {
        return sendToKafkaWithHeaders(notification, topic, null);
    }

    private boolean sendToKafkaWithHeaders(Notification notification, String topic,
                                           Map<String, String> headers) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // If in a transaction, register synchronization to send after commit
            try {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        performKafkaSend(notification, topic, headers);
                    }
                });
                return true;
            } catch (Exception e) {
                logAndRecordError(notification, topic, "tx_sync_error", e);
                return false;
            }
        } else {
            // If not in transaction, send immediately
            return performKafkaSend(notification, topic, headers);
        }
    }

    private boolean performKafkaSend(Notification notification, String topic,
                                     Map<String, String> headers) {
        long startTime = System.currentTimeMillis();

        try {
            String payload = objectMapper.writeValueAsString(notification);
            String key = notification.getIdempotencyKey() != null ?
                    notification.getIdempotencyKey() :
                    notification.getUserId() + "-" + notification.getId();

            log.debug("Sending to Kafka topic [{}]: {}", topic, payload);

            CompletableFuture<SendResult<String, String>> future;
            if (headers != null && !headers.isEmpty()) {
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, key, payload);
                headers.forEach((name, value) ->
                        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8)));
                future = kafkaTemplate.send(record);
            } else {
                future = kafkaTemplate.send(topic, key, payload);
            }

            SendResult<String, String> result = future.get(5, TimeUnit.SECONDS);

            metricsUtil.incrementCounter("notification.kafka.sent",
                    Map.of("topic", topic, "status", "success"));
            metricsUtil.recordLatency("notification.kafka.latency",
                    System.currentTimeMillis() - startTime);

            log.info("Successfully sent notification {} to topic {} (partition={}, offset={})",
                    notification.getId(), topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logAndRecordError(notification, topic, "interrupted", e);
            return false;
        } catch (ExecutionException e) {
            logAndRecordError(notification, topic, "execution_failed", e);
            return false;
        } catch (TimeoutException e) {
            logAndRecordError(notification, topic, "timeout", e);
            return false;
        } catch (Exception e) {
            logAndRecordError(notification, topic, "error", e);
            return false;
        }
    }

    private void logAndRecordError(Notification notification, String topic,
                                   String errorType, Exception e) {
        log.error("Failed to send notification {} to topic {}: {} error",
                notification.getId(), topic, errorType, e);

        metricsUtil.incrementCounter("notification.kafka.sent",
                Map.of("topic", topic, "status", "failure", "error_type", errorType));
    }

    @Override
    public void sendNotification(Notification notification) {
        boolean success = false;

        switch (notification.getType()) {
            case EMAIL:
                success = sendToEmailTopic(notification);
                break;
            case SMS:
                success = sendToSmsTopic(notification);
                break;
            case PUSH:
                success = sendToPushTopic(notification);
                break;
            default:
                log.error("Unknown notification type: {}", notification.getType());
                return;
        }

        if (!success) {
            log.error("Failed to send notification {} to Kafka", notification.getId());
            metricsUtil.incrementCounter("notification.kafka.failed",
                    Map.of("type", notification.getType().toString()));
        } else {
            metricsUtil.incrementCounter("notification.kafka.success",
                    Map.of("type", notification.getType().toString()));
        }
    }
}