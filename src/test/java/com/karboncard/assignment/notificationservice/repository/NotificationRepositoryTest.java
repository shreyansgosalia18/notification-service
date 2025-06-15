package com.karboncard.assignment.notificationservice.repository;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

//    @Autowired
//    private NotificationRepository notificationRepository;
//
//    private Notification notification;
//
//    @BeforeEach
//    void setUp() {
//        notification = new Notification();
//        notification.setId("notif-1");
//        notification.setUserId("user1");
//        notification.setType(NotificationType.EMAIL);
//        notification.setPriority(NotificationPriority.HIGH);
//        notification.setStatus(NotificationStatus.PENDING);
//        notification.setTemplateId("TEMPLATE1");
//        notification.setIdempotencyKey("idem-key-1");
//        notification.setCreatedAt(LocalDateTime.now().minusMinutes(10));
//        notification.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
//        notification.setDeliveryAttempts(0);
//        // Add any other required fields if needed
//
//        notificationRepository.save(notification);
//    }
//
//    @Test
//    @DisplayName("Find by userId returns notifications")
//    void testFindByUserId() {
//        List<Notification> notifications = notificationRepository.findByUserId("user1");
//        assertThat(notifications).isNotEmpty();
//        assertThat(notifications.get(0).getUserId()).isEqualTo("user1");
//    }
//
//    @Test
//    @DisplayName("Find by userId paginated returns notifications")
//    void testFindByUserIdPaginated() {
//        Page<Notification> page = notificationRepository.findByUserId("user1", PageRequest.of(0, 10));
//        assertThat(page.getContent()).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("Find by correlationId returns empty if not present")
//    void testFindByCorrelationId() {
//        List<Notification> found = notificationRepository.findByCorrelationId("no-such-correlation");
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    @DisplayName("Find by idempotencyKey returns notification")
//    void testFindByIdempotencyKey() {
//        Optional<Notification> found = notificationRepository.findByIdempotencyKey("idem-key-1");
//        assertThat(found).isPresent();
//        assertThat(found.get().getId()).isEqualTo("notif-1");
//    }
//
//    @Test
//    @DisplayName("Find by userId and status with pagination")
//    void testFindByUserIdAndStatus() {
//        Page<Notification> page = notificationRepository.findByUserIdAndStatus("user1", NotificationStatus.PENDING, PageRequest.of(0, 10));
//        assertThat(page).isNotNull();
//        assertThat(page.getContent()).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("Find retryable notifications")
//    void testFindRetryableNotifications() {
//        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
//        List<Notification> retryables = notificationRepository.findRetryableNotifications(
//                NotificationStatus.PENDING, cutoff, 3, PageRequest.of(0, 10));
//        assertThat(retryables).isNotEmpty();
//        assertThat(retryables.get(0).getId()).isEqualTo("notif-1");
//    }
//
//    @Test
//    @DisplayName("Update notification status")
//    void testUpdateNotificationStatus() {
//        int updated = notificationRepository.updateNotificationStatus(
//                "notif-1", NotificationStatus.SENT, LocalDateTime.now());
//        assertThat(updated).isEqualTo(1);
//
//        Notification updatedNotif = notificationRepository.findById("notif-1").get();
//        assertThat(updatedNotif.getStatus()).isEqualTo(NotificationStatus.SENT);
//    }
//
//    @Test
//    @DisplayName("Update after delivery attempt")
//    void testUpdateAfterDeliveryAttempt() {
//        int updated = notificationRepository.updateAfterDeliveryAttempt(
//                "notif-1", NotificationStatus.FAILED, LocalDateTime.now(), "error!");
//        assertThat(updated).isEqualTo(1);
//
//        Notification notif = notificationRepository.findById("notif-1").get();
//        assertThat(notif.getStatus()).isEqualTo(NotificationStatus.FAILED);
//        assertThat(notif.getErrorMessage()).isEqualTo("error!");
//    }
//
//    @Test
//    @DisplayName("Find by status and priority ordered by createdAt")
//    void testFindByStatusAndPriorityOrderByCreatedAtAsc() {
//        List<Notification> found = notificationRepository.findByStatusAndPriorityOrderByCreatedAtAsc(
//                NotificationStatus.PENDING, NotificationPriority.HIGH, PageRequest.of(0, 10));
//        assertThat(found).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("Count by userId and status")
//    void testCountByUserIdAndStatus() {
//        long count = notificationRepository.countByUserIdAndStatus("user1", NotificationStatus.PENDING);
//        assertThat(count).isGreaterThanOrEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("Find by userId and type paginated")
//    void testFindByUserIdAndType() {
//        Page<Notification> page = notificationRepository.findByUserIdAndType(
//                "user1", NotificationType.EMAIL, PageRequest.of(0, 10));
//        assertThat(page.getContent()).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("Find by type and status")
//    void testFindByTypeAndStatus() {
//        List<Notification> found = notificationRepository.findByTypeAndStatus(
//                NotificationType.EMAIL, NotificationStatus.PENDING, PageRequest.of(0, 10));
//        assertThat(found).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("Find failed notifications for DLQ")
//    void testFindFailedNotificationsForDLQ() {
//        // Update for failure and max delivery attempts
//        notification.setStatus(NotificationStatus.FAILED);
//        notification.setDeliveryAttempts(3);
//        notificationRepository.save(notification);
//
//        List<Notification> dlq = notificationRepository.findFailedNotificationsForDLQ(
//                NotificationStatus.FAILED, 3, PageRequest.of(0, 10));
//        assertThat(dlq).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("Count recent notifications by user")
//    void testCountRecentNotificationsByUser() {
//        long count = notificationRepository.countRecentNotificationsByUser(
//                "user1", LocalDateTime.now().minusHours(1));
//        assertThat(count).isGreaterThanOrEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("Count recent notifications by user and template")
//    void testCountRecentNotificationsByUserAndTemplate() {
//        long count = notificationRepository.countRecentNotificationsByUserAndTemplate(
//                "user1", "TEMPLATE1", LocalDateTime.now().minusHours(1));
//        assertThat(count).isGreaterThanOrEqualTo(1);
//    }
}