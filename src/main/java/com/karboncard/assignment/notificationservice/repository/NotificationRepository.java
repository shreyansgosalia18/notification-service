package com.karboncard.assignment.notificationservice.repository;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Find notifications by user ID (for service layer use)
     */
    List<Notification> findByUserId(String userId);

    /**
     * Find notifications by user ID with pagination (for API endpoints)
     */
    Page<Notification> findByUserId(String userId, Pageable pageable);

    /**
     * Find notifications by correlation ID
     */
    List<Notification> findByCorrelationId(String correlationId);

    /**
     * Find notification by idempotency key for exactly-once delivery guarantees
     */
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find notifications by user ID and status with pagination
     */
    Page<Notification> findByUserIdAndStatus(String userId, NotificationStatus status, Pageable pageable);

    /**
     * Find notifications eligible for retry based on status, last attempt time, and max attempts
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.lastAttemptedAt < :cutoffTime AND n.deliveryAttempts < :maxAttempts")
    List<Notification> findRetryableNotifications(
            @Param("status") NotificationStatus status,
            @Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("maxAttempts") int maxAttempts,
            Pageable pageable);

    /**
     * Update notification status
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :newStatus, n.updatedAt = :now WHERE n.id = :id")
    int updateNotificationStatus(@Param("id") String id, @Param("newStatus") NotificationStatus newStatus, @Param("now") LocalDateTime now);

    /**
     * Update notification after a delivery attempt
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.lastAttemptedAt = :attemptTime, " +
            "n.deliveryAttempts = n.deliveryAttempts + 1, n.updatedAt = :attemptTime, " +
            "n.errorMessage = :errorMessage WHERE n.id = :id")
    int updateAfterDeliveryAttempt(
            @Param("id") String id,
            @Param("status") NotificationStatus status,
            @Param("attemptTime") LocalDateTime attemptTime,
            @Param("errorMessage") String errorMessage);

    /**
     * Find notifications by status and priority (for worker processing)
     */
    List<Notification> findByStatusAndPriorityOrderByCreatedAtAsc(NotificationStatus status, NotificationPriority priority, Pageable pageable);

    /**
     * Count notifications by user ID and status (for rate limiting and metrics)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") NotificationStatus status);

    /**
     * Find notifications by user ID and type (for filtering in APIs)
     */
    Page<Notification> findByUserIdAndType(String userId, NotificationType type, Pageable pageable);

    /**
     * Find notifications by type for specific notification type consumers
     */
    List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status, Pageable pageable);

    /**
     * Find notifications for dead letter queue after max retries
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.deliveryAttempts >= :maxAttempts")
    List<Notification> findFailedNotificationsForDLQ(
            @Param("status") NotificationStatus status,
            @Param("maxAttempts") int maxAttempts,
            Pageable pageable);

    /**
     * Count recent notifications by user ID within a time window (for rate limiting)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.createdAt > :since")
    long countRecentNotificationsByUser(
            @Param("userId") String userId,
            @Param("since") LocalDateTime since);

    /**
     * Count recent notifications by user ID and template ID within a time window (for template-specific rate limiting)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.templateId = :templateId AND n.createdAt > :since")
    long countRecentNotificationsByUserAndTemplate(
            @Param("userId") String userId,
            @Param("templateId") String templateId,
            @Param("since") LocalDateTime since);
}