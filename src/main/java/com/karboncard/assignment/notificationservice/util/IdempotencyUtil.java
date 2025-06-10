package com.karboncard.assignment.notificationservice.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating and validating idempotency keys
 * to ensure exactly-once delivery semantics
 */
@Component
public class IdempotencyUtil {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyUtil.class);

    @Autowired
    private NotificationRepository notificationRepository;

    // In-memory cache for processed notifications
    private final Map<String, Boolean> processedNotifications = new ConcurrentHashMap<>();

    /**
     * Generates a random idempotency key
     *
     * @return A unique idempotency key
     */
    public String generateKey() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a deterministic idempotency key based on the content
     * Useful when the client doesn't provide a key but idempotency is still needed
     *
     * @param userId User ID
     * @param templateId Template ID
     * @param content Content that uniquely identifies the notification
     * @return A deterministic idempotency key
     */
    public String generateDeterministicKey(String userId, String templateId, String content) {
        try {
            String input = userId + ":" + templateId + ":" + content;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating deterministic idempotency key", e);
            return generateKey(); // Fallback to random key
        }
    }

    /**
     * Validates if an idempotency key has the expected format
     *
     * @param key The idempotency key to validate
     * @return True if the key is valid, false otherwise
     */
    public boolean isValidKey(String key) {
        // Simple validation - can be extended based on requirements
        return key != null && !key.trim().isEmpty();
    }

    /**
     * Checks if a notification has already been successfully processed
     *
     * @param notificationId ID of the notification to check
     * @return true if already processed, false otherwise
     */
    public boolean isAlreadyProcessed(String notificationId) {
        // First check in-memory cache for performance
        if (processedNotifications.containsKey(notificationId)) {
            return true;
        }

        // Check database as fallback (for resilience across service restarts)
        return notificationRepository.findById(notificationId)
                .map(notification -> notification.getStatus() == NotificationStatus.SENT)
                .orElse(false);
    }

    /**
     * Marks a notification as successfully processed
     *
     * @param notificationId ID of the notification to mark as processed
     */
    public void markAsProcessed(String notificationId) {
        processedNotifications.put(notificationId, true);
        log.debug("Marked notification {} as processed for idempotency", notificationId);
    }
}