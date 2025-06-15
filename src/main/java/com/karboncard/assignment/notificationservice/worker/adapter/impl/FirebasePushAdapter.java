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
        // MOCK: No real push notification will be sent.
        String deviceToken = notification.getTemplateParams().containsKey("deviceToken")
                ? notification.getTemplateParams().get("deviceToken").toString()
                : notification.getUserId() + "_device";

        String title = notification.getTemplateParams().containsKey("title")
                ? notification.getTemplateParams().get("title").toString()
                : "Notification: " + notification.getTemplateId();

        String body = notification.getTemplateParams().containsKey("body")
                ? notification.getTemplateParams().get("body").toString()
                : "You have a new notification";

        log.info("MOCK: Sending Push notification via Firebase to device {}: {} (notificationId={}, templateParams={})",
                deviceToken, title, notification.getId(), notification.getTemplateParams());

        if (!notification.getTemplateParams().containsKey("deviceToken")) {
            log.warn("MOCK: deviceToken missing in templateParams, used fallback for notificationId={}", notification.getId());
        }

        // Deterministic: fail only if "body" is "FAIL"
        if ("FAIL".equalsIgnoreCase(body)) {
            log.warn("MOCK: Deterministic failure sending Push notification to device {} (notificationId={})", deviceToken, notification.getId());
            return false;
        }
        return true;
    }
}