package com.karboncard.assignment.notificationservice.service;

import com.karboncard.assignment.notificationservice.model.entity.Notification;

public interface KafkaProducerService {
    /**
     * Sends a notification to the email topic
     * @param notification The notification to send
     * @return true if successfully sent to Kafka, false otherwise
     */
    boolean sendToEmailTopic(Notification notification);

    /**
     * Sends a notification to the SMS topic
     * @param notification The notification to send
     * @return true if successfully sent to Kafka, false otherwise
     */
    boolean sendToSmsTopic(Notification notification);

    /**
     * Sends a notification to the push notification topic
     * @param notification The notification to send
     * @return true if successfully sent to Kafka, false otherwise
     */
    boolean sendToPushTopic(Notification notification);

    boolean sendWithPriority(Notification notification, String topic);

    void sendNotification(Notification notification);
}