package com.karboncard.assignment.notificationservice.worker.adapter;

import com.karboncard.assignment.notificationservice.model.entity.Notification;

public interface ExternalProviderAdapter {
    boolean sendNotification(Notification notification);
}