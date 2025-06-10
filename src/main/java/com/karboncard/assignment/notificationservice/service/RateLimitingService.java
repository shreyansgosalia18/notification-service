package com.karboncard.assignment.notificationservice.service;

import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;

public interface RateLimitingService {

    /**
     * Check if the user has exceeded their overall notification rate limit
     *
     * @param userId The user ID to check
     * @throws RateLimitExceededException if the user has exceeded their rate limit
     */
    void checkUserRateLimit(String userId) throws RateLimitExceededException;

    /**
     * Check if the user has exceeded their rate limit for a specific template type
     *
     * @param userId The user ID to check
     * @param templateId The template ID to check
     * @throws RateLimitExceededException if the user has exceeded their template-specific rate limit
     */
    void checkTemplateRateLimit(String userId, String templateId) throws RateLimitExceededException;

    /**
     * Record a notification attempt for a user (call after successful processing)
     *
     * @param userId The user ID
     */
    void recordUserNotificationAttempt(String userId);

    /**
     * Record a template-specific notification attempt for a user (call after successful processing)
     *
     * @param userId The user ID
     * @param templateId The template ID
     */
    void recordTemplateNotificationAttempt(String userId, String templateId);

}