package com.karboncard.assignment.notificationservice.worker.adapter.impl;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.worker.adapter.ExternalProviderAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SendGridEmailAdapter implements ExternalProviderAdapter {

    @Override
    public boolean sendNotification(Notification notification) {
        // Extract email from templateParams
        String emailAddress = notification.getTemplateParams().containsKey("email")
                ? notification.getTemplateParams().get("email").toString()
                : notification.getUserId() + "@example.com"; // Fallback

        // Extract subject from templateParams or create default
        String subject = notification.getTemplateParams().containsKey("subject")
                ? notification.getTemplateParams().get("subject").toString()
                : "Notification: " + notification.getTemplateId();

        // Extract content from templateParams
        String content = notification.getTemplateParams().containsKey("content")
                ? notification.getTemplateParams().get("content").toString()
                : "Notification details for template: " + notification.getTemplateId();

        log.info("Sending Email via SendGrid to {}, Subject: {}", emailAddress, subject);

        // Simulate occasional failures (15% chance)
        boolean success = Math.random() > 0.15;
        if (!success) {
            log.warn("Failed to send Email to {}", emailAddress);
        }

        return success;
    }
}