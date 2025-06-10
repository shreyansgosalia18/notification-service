package com.karboncard.assignment.notificationservice.worker.impl;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.repository.NotificationRepository;
import com.karboncard.assignment.notificationservice.util.TransactionRetryHelper;
import com.karboncard.assignment.notificationservice.worker.NotificationConsumer;
import com.karboncard.assignment.notificationservice.worker.adapter.impl.FirebasePushAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationConsumer implements NotificationConsumer {

    private final FirebasePushAdapter pushAdapter;
    private final NotificationRepository notificationRepository;
    private final TransactionRetryHelper transactionRetryHelper;

    @Override
    @Retryable(value = Exception.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public void processNotification(Notification notification) {
        String notificationId = notification.getId();
        log.info("Processing push notification: {}", notificationId);

        try {
            // Update notification to processing
            transactionRetryHelper.updateNotificationWithRetry(notificationId, n -> {
                n.setStatus(NotificationStatus.RETRYING);
                n.setLastAttemptedAt(LocalDateTime.now());
                n.setDeliveryAttempts(n.getDeliveryAttempts() + 1);
                n.setUpdatedAt(LocalDateTime.now());
            });

            // Fetch fresh notification after status update
            Notification freshNotification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new IllegalStateException("Notification not found"));

            // Send notification through adapter
            boolean success = pushAdapter.sendNotification(freshNotification);

            if (success) {
                transactionRetryHelper.updateNotificationWithRetry(notificationId, n -> {
                    n.setStatus(NotificationStatus.SENT);
                    n.setUpdatedAt(LocalDateTime.now());
                });
                log.info("Push notification {} sent successfully", notificationId);
            } else {
                handleFailure(notificationId, "Push provider returned failure");
            }
        } catch (Exception e) {
            handleFailure(notificationId, e.getMessage());
            throw e; // Rethrow to trigger retry
        }
    }

    private void handleFailure(String notificationId, String errorMessage) {
        log.error("Failed to process push notification {}: {}", notificationId, errorMessage);

        transactionRetryHelper.updateNotificationWithRetry(notificationId, n -> {
            if (n.getDeliveryAttempts() >= 3) {
                n.setStatus(NotificationStatus.PERMANENT_FAILURE);
            } else {
                n.setStatus(NotificationStatus.FAILED);
            }
            n.setErrorMessage(errorMessage);
            n.setLastAttemptedAt(LocalDateTime.now());
            n.setUpdatedAt(LocalDateTime.now());
        });
    }
}