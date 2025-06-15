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
        // MOCK: No real SMS will be sent.
        String phoneNumber = notification.getTemplateParams().containsKey("phoneNumber")
                ? notification.getTemplateParams().get("phoneNumber").toString()
                : notification.getUserId(); // Fallback

        String message = notification.getTemplateParams().containsKey("content")
                ? notification.getTemplateParams().get("content").toString()
                : "Notification for template: " + notification.getTemplateId();

        log.info("MOCK: Sending SMS via Twilio to {}: {} (notificationId={}, templateParams={})",
                phoneNumber, message, notification.getId(), notification.getTemplateParams());

        if (!notification.getTemplateParams().containsKey("phoneNumber")) {
            log.warn("MOCK: phoneNumber missing in templateParams, used userId as fallback for notificationId={}", notification.getId());
        }

        // Simulate occasional failure for assignment (20% chance)
        boolean success = Math.random() > 0.2;
        if (!success) {
            log.warn("MOCK: Simulated failure sending SMS to {} (notificationId={})", phoneNumber, notification.getId());
        }
        return success;
    }
}