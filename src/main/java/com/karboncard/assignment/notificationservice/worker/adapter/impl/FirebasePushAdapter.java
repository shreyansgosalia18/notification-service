package com.karboncard.assignment.notificationservice.worker.adapter.impl;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.worker.adapter.ExternalProviderAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirebasePushAdapter implements ExternalProviderAdapter {

    @Override
    public boolean sendNotification(Notification notification) {
        // Extract device token from templateParams
        String deviceToken = notification.getTemplateParams().containsKey("deviceToken")
                ? notification.getTemplateParams().get("deviceToken").toString()
                : notification.getUserId() + "_device"; // Fallback

        // Extract title from templateParams
        String title = notification.getTemplateParams().containsKey("title")
                ? notification.getTemplateParams().get("title").toString()
                : "Notification: " + notification.getTemplateId();

        // Extract body from templateParams
        String body = notification.getTemplateParams().containsKey("body")
                ? notification.getTemplateParams().get("body").toString()
                : "You have a new notification";

        log.info("Sending Push notification via Firebase to device {}: {}", deviceToken, title);

        // Simulate occasional failures (10% chance)
        boolean success = Math.random() > 0.1;
        if (!success) {
            log.warn("Failed to send Push notification to device {}", deviceToken);
        }

        return success;
    }
}