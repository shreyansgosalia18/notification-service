package com.karboncard.assignment.notificationservice.util;

import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdempotencyUtilTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private IdempotencyUtil idempotencyUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateKey_returnsUniqueNonNullKeys() {
        String key1 = idempotencyUtil.generateKey();
        String key2 = idempotencyUtil.generateKey();

        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2, "Generated keys should be unique");
        assertTrue(idempotencyUtil.isValidKey(key1));
        assertTrue(idempotencyUtil.isValidKey(key2));
    }

    @Test
    void generateDeterministicKey_sameInput_sameOutput() {
        String userId = "user1";
        String templateId = "template1";
        String content = "hello world";
        String key1 = idempotencyUtil.generateDeterministicKey(userId, templateId, content);
        String key2 = idempotencyUtil.generateDeterministicKey(userId, templateId, content);

        assertNotNull(key1);
        assertEquals(key1, key2, "Deterministic keys should be the same for identical input");
        assertTrue(idempotencyUtil.isValidKey(key1));
    }

    @Test
    void generateDeterministicKey_differentInput_differentOutput() {
        String key1 = idempotencyUtil.generateDeterministicKey("user1", "template1", "abc");
        String key2 = idempotencyUtil.generateDeterministicKey("user1", "template1", "def");
        assertNotEquals(key1, key2, "Deterministic keys should differ for different input");
    }

    @Test
    void isValidKey_checksVariousCases() {
        assertTrue(idempotencyUtil.isValidKey("abc"));
        assertFalse(idempotencyUtil.isValidKey(""));
        assertFalse(idempotencyUtil.isValidKey("   "));
        assertFalse(idempotencyUtil.isValidKey(null));
    }

    @Test
    void isAlreadyProcessed_returnsTrue_whenInMemory() {
        String notificationId = "notif-1";
        idempotencyUtil.markAsProcessed(notificationId);

        assertTrue(idempotencyUtil.isAlreadyProcessed(notificationId));
        // Should NOT call the repository if found in cache
        verify(notificationRepository, never()).findById(any());
    }

    @Test
    void isAlreadyProcessed_returnsTrue_whenInDbAndStatusSent() {
        String notificationId = "notif-2";
        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.SENT);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertTrue(idempotencyUtil.isAlreadyProcessed(notificationId));
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    void isAlreadyProcessed_returnsFalse_whenInDbAndStatusNotSent() {
        String notificationId = "notif-3";
        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertFalse(idempotencyUtil.isAlreadyProcessed(notificationId));
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    void isAlreadyProcessed_returnsFalse_whenNotInCacheOrDb() {
        String notificationId = "notif-4";
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertFalse(idempotencyUtil.isAlreadyProcessed(notificationId));
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    void markAsProcessed_setsCacheAndDebugLog() {
        String notificationId = "notif-5";
        idempotencyUtil.markAsProcessed(notificationId);

        assertTrue(idempotencyUtil.isAlreadyProcessed(notificationId));
    }
}