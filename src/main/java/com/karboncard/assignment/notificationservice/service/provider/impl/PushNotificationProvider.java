package com.karboncard.assignment.notificationservice.service.provider.impl;

import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import com.karboncard.assignment.notificationservice.service.KafkaProducerService;
import com.karboncard.assignment.notificationservice.service.provider.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationProvider implements NotificationProvider {

    private final KafkaProducerService kafkaProducerService;
    private static final int MAX_PAYLOAD_SIZE = 4096; // Common payload limit for push notifications
    private static final Set<String> SUPPORTED_PLATFORMS = Set.of("IOS", "ANDROID", "WEB");

    @Override
    public boolean send(Notification notification, NotificationRequestDTO requestDTO) {
        log.info("Processing push notification for user {}, template: {}",
                notification.getUserId(), notification.getTemplateId());

        // Get device token from templateParams
        Map<String, Object> params = notification.getTemplateParams();
        if (params == null) {
            log.error("Missing templateParams for notification {}", notification.getId());
            return false;
        }

        // Validate device token
        String deviceToken = params.containsKey("deviceToken") ? params.get("deviceToken").toString() : null;
        if (deviceToken == null || deviceToken.isEmpty()) {
            log.error("Missing device token for notification {}", notification.getId());
            return false;
        }

        // Check platform type
        String platform = params.containsKey("platform") && params.get("platform") instanceof String ?
                (String) params.get("platform") : "UNKNOWN";

        if (!SUPPORTED_PLATFORMS.contains(platform.toUpperCase())) {
            log.error("Unsupported platform '{}' for notification {}", platform, notification.getId());
            return false;
        }

        // Format platform-specific payload
        formatPlatformSpecificPayload(params, platform.toUpperCase());

        // Check payload size
        if (params.containsKey("content")) {
            String content = params.get("content").toString();
            if (content.length() > MAX_PAYLOAD_SIZE) {
                log.warn("Push notification payload exceeds size limit ({} bytes), truncating for notification {}",
                        content.length(), notification.getId());
                params.put("content", content.substring(0, MAX_PAYLOAD_SIZE));
            }
        }

        // Handle priority (especially important for push notifications)
        if (NotificationPriority.HIGH.equals(notification.getPriority())) {
            log.info("Processing high priority push notification for user {}", notification.getUserId());
            params.put("priority", "high");
        }

        return kafkaProducerService.sendToPushTopic(notification);
    }

    private void formatPlatformSpecificPayload(Map<String, Object> params, String platform) {
        // Format payload based on platform requirements
        switch (platform) {
            case "IOS":
                // iOS specific payload structuring (APNS format)
                if (!params.containsKey("aps")) {
                    Map<String, Object> aps = new HashMap<>();
                    aps.put("alert", params.getOrDefault("title", ""));
                    aps.put("sound", "default");
                    params.put("aps", aps);
                }
                break;
            case "ANDROID":
                // Android specific payload structuring (FCM format)
                if (!params.containsKey("data")) {
                    params.put("data", new HashMap<>(params));
                }
                break;
            case "WEB":
                // Web push notification formatting
                if (!params.containsKey("notification")) {
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("title", params.getOrDefault("title", ""));
                    notification.put("body", params.getOrDefault("body", ""));
                    params.put("notification", notification);
                }
                break;
        }

        log.debug("Formatted payload for {} platform", platform);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }
}