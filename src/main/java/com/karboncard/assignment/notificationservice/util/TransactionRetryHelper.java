package com.karboncard.assignment.notificationservice.util;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

@Component
@Slf4j
public class TransactionRetryHelper {

    private final PlatformTransactionManager transactionManager;
    private final NotificationRepository notificationRepository;

    public TransactionRetryHelper(PlatformTransactionManager transactionManager,
                                  NotificationRepository notificationRepository) {
        this.transactionManager = transactionManager;
        this.notificationRepository = notificationRepository;
    }

    public void updateNotificationWithRetry(String notificationId, Consumer<Notification> updater) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        int maxRetries = 5;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                template.execute(status -> {
                    Notification freshNotification = notificationRepository.findById(notificationId)
                            .orElse(null);

                    if (freshNotification == null) {
                        log.warn("Notification not found: {}", notificationId);
                        return null;
                    }

                    updater.accept(freshNotification);
                    return notificationRepository.save(freshNotification);
                });
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(100L * (long)Math.pow(2, attempt));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}