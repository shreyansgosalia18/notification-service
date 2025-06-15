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
public class EmailNotificationProvider implements NotificationProvider {

    private final KafkaProducerService kafkaProducerService;
    // RFC 5322 compliant email regex
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    private static final int MAX_SUBJECT_LENGTH = 255;

    @Override
    public boolean send(Notification notification, NotificationRequestDTO requestDTO) {
        log.info("Processing email notification for user {}, template: {}",
                notification.getUserId(), notification.getTemplateId());

        // Get email address from templateParams
        Map<String, Object> params = notification.getTemplateParams();
        if (params == null || !params.containsKey("email")) {
            String msg = String.format("Missing email address in templateParams for notification %s. templateParams: %s", notification.getId(), params);
            log.error(msg);
            notification.setStatus(NotificationStatus.PERMANENT_FAILURE);
            notification.setErrorMessage(msg);
            return false;
        }

        String emailAddress = params.get("email").toString();

        // Validate email address
        if (emailAddress == null || !Pattern.matches(EMAIL_REGEX, emailAddress)) {
            String msg = String.format("Invalid email address format for notification %s. Provided: %s", notification.getId(), emailAddress);
            log.error(msg);
            notification.setStatus(NotificationStatus.PERMANENT_FAILURE);
            notification.setErrorMessage(msg);
            return false;
        }

        // Extract subject from templateParams if available
        if (params.containsKey("subject") && params.get("subject") instanceof String) {
            String subject = (String) params.get("subject");
            if (subject.length() > MAX_SUBJECT_LENGTH) {
                log.warn("Email subject exceeds maximum length ({} chars), truncating for notification {}",
                        subject.length(), notification.getId());
                params.put("subject", subject.substring(0, MAX_SUBJECT_LENGTH));
            }
        }

        // Check for attachments (if supported)
        if (params.containsKey("attachments")) {
            log.info("Email contains attachments for notification {}", notification.getId());
            // Validate attachment size, format, etc. if needed
        }

        // Handle priority for email delivery sequence
        if (NotificationPriority.HIGH.equals(notification.getPriority())) {
            log.info("Processing high priority email for user {}", notification.getUserId());
            // Could implement expedited delivery for high priority emails
        } else if (NotificationPriority.LOW.equals(notification.getPriority())) {
            log.info("Processing low priority email for user {}", notification.getUserId());
            // Could batch low priority emails for efficient delivery
        }

        return kafkaProducerService.sendToEmailTopic(notification);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}