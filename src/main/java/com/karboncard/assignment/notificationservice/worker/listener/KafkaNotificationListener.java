package com.karboncard.assignment.notificationservice.worker.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.worker.impl.EmailNotificationConsumer;
import com.karboncard.assignment.notificationservice.worker.impl.SmsNotificationConsumer;
import com.karboncard.assignment.notificationservice.worker.impl.PushNotificationConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationListener {

    private final ObjectMapper objectMapper;
    private final EmailNotificationConsumer emailConsumer;
    private final SmsNotificationConsumer smsConsumer;
    private final PushNotificationConsumer pushConsumer;

    @KafkaListener(topics = "notification-email-topic", groupId = "email-notification-group")
    public void consumeEmailNotifications(String message) throws JsonProcessingException {
        log.info("Received email notification message: {}", message);
        Notification notification = objectMapper.readValue(message, Notification.class);
        emailConsumer.processNotification(notification);
    }

    @KafkaListener(topics = "notification-sms-topic", groupId = "sms-notification-group")
    public void consumeSmsNotifications(String message) throws JsonProcessingException {
        log.info("Received SMS notification message: {}", message);
        Notification notification = objectMapper.readValue(message, Notification.class);
        smsConsumer.processNotification(notification);
    }

    @KafkaListener(topics = "notification-push-topic", groupId = "push-notification-group")
    public void consumePushNotifications(String message) throws JsonProcessingException {
        log.info("Received push notification message: {}", message);
        Notification notification = objectMapper.readValue(message, Notification.class);
        pushConsumer.processNotification(notification);
    }
}