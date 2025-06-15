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
        // MOCK: No real email will be sent.
        String emailAddress = notification.getTemplateParams().containsKey("email")
                ? notification.getTemplateParams().get("email").toString()
                : notification.getUserId() + "@example.com"; // Fallback

        String subject = notification.getTemplateParams().containsKey("subject")
                ? notification.getTemplateParams().get("subject").toString()
                : "Notification: " + notification.getTemplateId();

        String content = notification.getTemplateParams().containsKey("content")
                ? notification.getTemplateParams().get("content").toString()
                : "Notification details for template: " + notification.getTemplateId();

        log.info("MOCK: Sending Email via SendGrid to {}, Subject: {} (notificationId={}, templateParams={})",
                emailAddress, subject, notification.getId(), notification.getTemplateParams());

        if (!notification.getTemplateParams().containsKey("email")) {
            log.warn("MOCK: email missing in templateParams, used userId as fallback for notificationId={}", notification.getId());
        }

        // Deterministic: fail only if "content" is "FAIL"
        if ("FAIL".equalsIgnoreCase(content)) {
            log.warn("MOCK: Deterministic failure sending Email to {} (notificationId={})", emailAddress, notification.getId());
            return false;
        }
        return true;
    }
}