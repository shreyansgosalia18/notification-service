package com.karboncard.assignment.notificationservice.worker;

import com.karboncard.assignment.notificationservice.model.entity.Notification;

public interface NotificationConsumer {
    void processNotification(Notification notification);
}