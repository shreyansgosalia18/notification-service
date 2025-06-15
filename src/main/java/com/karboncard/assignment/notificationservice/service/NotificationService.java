package com.karboncard.assignment.notificationservice.service;

import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;
import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.dto.response.NotificationResponseDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationService {

    /**
     * Processes a notification request, applying validations and rate limits
     * before asynchronously dispatching it
     *
     * @param requestDTO The notification request
     * @return Response containing status and notification ID
     */
    NotificationResponseDTO processNotification(NotificationRequestDTO requestDTO)
            throws RateLimitExceededException;

    /**
     * Retrieves a notification by ID
     *
     * @param id The notification ID
     * @return The notification if found
     */
    Optional<Notification> getNotificationById(String id);

    /**
     * Retrieves notifications for a specific user
     *
     * @param userId The user ID
     * @return List of notifications for the user
     */
    List<Notification> getNotificationsByUserId(String userId);

    /**
     * Retrieves notifications by correlation ID
     *
     * @param correlationId The correlation ID
     * @return List of notifications with the given correlation ID
     */
    List<Notification> getNotificationsByCorrelationId(String correlationId);
}