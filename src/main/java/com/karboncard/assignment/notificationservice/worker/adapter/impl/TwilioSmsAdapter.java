package com.karboncard.assignment.notificationservice.worker.adapter.impl;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.worker.adapter.ExternalProviderAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwilioSmsAdapter implements ExternalProviderAdapter {

    @Override
    public boolean sendNotification(Notification notification) {
        // Get phone number from templateParams or use userId as fallback
        String phoneNumber = notification.getTemplateParams().containsKey("phoneNumber")
                ? notification.getTemplateParams().get("phoneNumber").toString()
                : notification.getUserId(); // Assuming userId could be phone number

        // Get message content from templateParams
        String message = notification.getTemplateParams().containsKey("content")
                ? notification.getTemplateParams().get("content").toString()
                : "Notification for template: " + notification.getTemplateId();

        log.info("Sending SMS via Twilio to {}: {}", phoneNumber, message);

        // Simulate occasional failures (20% chance)
        boolean success = Math.random() > 0.2;
        if (!success) {
            log.warn("Failed to send SMS to {}", phoneNumber);
        }

        return success;
    }
}