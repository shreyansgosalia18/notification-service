package com.karboncard.assignment.notificationservice.model.dao;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class NotificationDao {
//    private String id;
//    private String userId;
//    private NotificationType type;
//    private String templateId;
//    private Map<String, Object> templateParams;
//    private NotificationPriority priority;
//    private String correlationId;
//    private NotificationStatus status;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private LocalDateTime lastAttemptedAt;
//    private int deliveryAttempts;
//    private String errorMessage;
//
//    // Constructor
//    public NotificationDao() {
//        this.id = UUID.randomUUID().toString();
//        this.status = NotificationStatus.PENDING;
//        this.deliveryAttempts = 0;
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    // Convert Entity to DAO
//    public static NotificationDao fromEntity(Notification entity) {
//        NotificationDao dao = new NotificationDao();
//        dao.id = entity.getId();
//        dao.userId = entity.getUserId();
//        dao.type = entity.getType();
//        dao.templateId = entity.getTemplateId();
//        dao.templateParams = entity.getTemplateParams();
//        dao.priority = entity.getPriority();
//        dao.correlationId = entity.getCorrelationId();
//        dao.status = entity.getStatus();
//        dao.createdAt = entity.getCreatedAt();
//        dao.updatedAt = entity.getUpdatedAt();
//        dao.lastAttemptedAt = entity.getLastAttemptedAt();
//        dao.deliveryAttempts = entity.getDeliveryAttempts();
//        dao.errorMessage = entity.getErrorMessage();
//        return dao;
//    }
//
//    // Convert DAO to Entity
//    public Notification toEntity() {
//        Notification entity = new Notification();
//        entity.setId(this.id);
//        entity.setUserId(this.userId);
//        entity.setType(this.type);
//        entity.setTemplateId(this.templateId);
//        entity.setTemplateParams(this.templateParams);
//        entity.setPriority(this.priority);
//        entity.setCorrelationId(this.correlationId);
//        entity.setStatus(this.status);
//        entity.setCreatedAt(this.createdAt);
//        entity.setUpdatedAt(this.updatedAt);
//        entity.setLastAttemptedAt(this.lastAttemptedAt);
//        entity.setDeliveryAttempts(this.deliveryAttempts);
//        entity.setErrorMessage(this.errorMessage);
//        return entity;
//    }
//
//    // Business logic
//    public void incrementDeliveryAttempts() {
//        this.deliveryAttempts++;
//        this.lastAttemptedAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    // Getters and setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//
//    public NotificationType getType() { return type; }
//    public void setType(NotificationType type) { this.type = type; }
//
//    public String getTemplateId() { return templateId; }
//    public void setTemplateId(String templateId) { this.templateId = templateId; }
//
//    public Map<String, Object> getTemplateParams() { return templateParams; }
//    public void setTemplateParams(Map<String, Object> templateParams) { this.templateParams = templateParams; }
//
//    public NotificationPriority getPriority() { return priority; }
//    public void setPriority(NotificationPriority priority) { this.priority = priority; }
//
//    public String getCorrelationId() { return correlationId; }
//    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
//
//    public NotificationStatus getStatus() { return status; }
//    public void setStatus(NotificationStatus status) {
//        this.status = status;
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//
//    public LocalDateTime getLastAttemptedAt() { return lastAttemptedAt; }
//    public void setLastAttemptedAt(LocalDateTime lastAttemptedAt) { this.lastAttemptedAt = lastAttemptedAt; }
//
//    public int getDeliveryAttempts() { return deliveryAttempts; }
//    public void setDeliveryAttempts(int deliveryAttempts) { this.deliveryAttempts = deliveryAttempts; }
//
//    public String getErrorMessage() { return errorMessage; }
//    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}