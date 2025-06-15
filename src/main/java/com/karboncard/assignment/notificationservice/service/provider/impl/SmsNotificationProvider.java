package com.karboncard.assignment.notificationservice.service.provider.impl;

import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import com.karboncard.assignment.notificationservice.service.KafkaProducerService;
import com.karboncard.assignment.notificationservice.service.provider.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationProvider implements NotificationProvider {

    private final KafkaProducerService kafkaProducerService;
    private static final int SMS_CHARACTER_LIMIT = 160;
    // E.164 format phone number validation
    private static final String PHONE_NUMBER_REGEX = "^\\+?[1-9]\\d{1,14}$";

    @Override
    public boolean send(Notification notification, NotificationRequestDTO requestDTO) {
        log.info("Processing SMS notification for user {}, template: {}",
                notification.getUserId(), notification.getTemplateId());

        // Get phone number from templateParams
        Map<String, Object> params = notification.getTemplateParams();
        if (params == null || !params.containsKey("phoneNumber")) {
            String msg = String.format("Missing phone number in templateParams for notification %s. templateParams: %s", notification.getId(), params);
            log.error(msg);
            notification.setStatus(NotificationStatus.PERMANENT_FAILURE);
            notification.setErrorMessage(msg);
            return false;
        }

        String phoneNumber = params.get("phoneNumber").toString();

        // Validate phone number
        if (phoneNumber == null || !Pattern.matches(PHONE_NUMBER_REGEX, phoneNumber)) {
            String msg = String.format("Invalid phone number format for notification %s. Provided: %s", notification.getId(), phoneNumber);
            log.error(msg);
            notification.setStatus(NotificationStatus.PERMANENT_FAILURE);
            notification.setErrorMessage(msg);
            return false;
        }

        // Check message length if content is provided in templateParams
        if (params.containsKey("content")) {
            String content = params.get("content").toString();
            if (content.length() > SMS_CHARACTER_LIMIT) {
                log.warn("SMS content exceeds character limit ({} chars), truncating for notification {}",
                        content.length(), notification.getId());
                params.put("content", content.substring(0, SMS_CHARACTER_LIMIT));
            }
        }

        // Priority-based processing
        if (NotificationPriority.HIGH.equals(notification.getPriority())) {
            log.info("Processing high priority SMS for user {}", notification.getUserId());
            // Could implement dedicated high priority queue or channel
        }

        return kafkaProducerService.sendToSmsTopic(notification);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }
}