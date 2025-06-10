package com.karboncard.assignment.notificationservice.service.provider;

import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;

public interface NotificationProvider {
    /**
     * Sends a notification using the specific provider implementation
     * @param notification The notification entity to be sent
     * @param requestDTO The original request data
     * @return true if notification was successfully queued, false otherwise
     */
    boolean send(Notification notification, NotificationRequestDTO requestDTO);

    /**
     * Returns the notification type that this provider handles
     * @return NotificationType handled by this provider
     */
    NotificationType getType();
}