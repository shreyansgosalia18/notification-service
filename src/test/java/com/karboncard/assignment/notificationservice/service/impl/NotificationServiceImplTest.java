package com.karboncard.assignment.notificationservice.service.impl;

import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;
import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.dto.response.NotificationResponseDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import com.karboncard.assignment.notificationservice.repository.NotificationRepository;
import com.karboncard.assignment.notificationservice.service.KafkaProducerService;
import com.karboncard.assignment.notificationservice.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private RateLimitingService rateLimitingService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestDTO = NotificationRequestDTO.builder()
                .userId("test-user")
                .type(NotificationType.EMAIL)
                .templateId("WELCOME")
                .templateParams(Map.of("email", "test@test.com"))
                .priority(NotificationPriority.HIGH)
                .correlationId("corr-123")
                .build();
    }

    // --- Positive test: processNotification success ---
    @Test
    void processNotification_success() throws RateLimitExceededException {
        when(notificationRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        doNothing().when(rateLimitingService).checkUserRateLimit(anyString());
        doNothing().when(rateLimitingService).checkTemplateRateLimit(anyString(), anyString());

        Notification savedNotif = new Notification();
        savedNotif.setId("notif-1");
        savedNotif.setUserId(requestDTO.getUserId());
        savedNotif.setType(requestDTO.getType());
        savedNotif.setStatus(NotificationStatus.PENDING);
        savedNotif.setCreatedAt(LocalDateTime.now());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotif);

        doNothing().when(kafkaProducerService).sendNotification(any(Notification.class));

        NotificationResponseDTO response = notificationService.processNotification(requestDTO);

        assertNotNull(response);
        assertEquals("notif-1", response.getId());
        assertEquals(NotificationStatus.PENDING, response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals("Notification queued successfully", response.getMessage());
        verify(notificationRepository).save(any(Notification.class));
        verify(kafkaProducerService).sendNotification(any(Notification.class));
    }

    // --- Negative test: duplicate request (idempotency) ---
    @Test
    void processNotification_duplicateRequest() throws RateLimitExceededException {
        Notification existingNotif = new Notification();
        existingNotif.setId("notif-dup");
        existingNotif.setUserId(requestDTO.getUserId());
        existingNotif.setType(requestDTO.getType());
        existingNotif.setStatus(NotificationStatus.PENDING);
        existingNotif.setCreatedAt(LocalDateTime.now());
        when(notificationRepository.findByIdempotencyKey(anyString()))
                .thenReturn(Optional.of(existingNotif));

        NotificationResponseDTO response = notificationService.processNotification(requestDTO);

        assertNotNull(response);
        assertEquals("notif-dup", response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Duplicate request", response.getMessage());
        verify(notificationRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendNotification(any());
    }

    // --- Negative test: user rate limit exceeded ---
    @Test
    void processNotification_userRateLimitExceeded() throws RateLimitExceededException {
        when(notificationRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        doThrow(new RateLimitExceededException("User rate limit exceeded"))
                .when(rateLimitingService).checkUserRateLimit(anyString());

        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> notificationService.processNotification(requestDTO));
        assertEquals("User rate limit exceeded", ex.getMessage());
        verify(notificationRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendNotification(any());
    }

    // --- Negative test: template rate limit exceeded ---
    @Test
    void processNotification_templateRateLimitExceeded() throws RateLimitExceededException {
        when(notificationRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        doNothing().when(rateLimitingService).checkUserRateLimit(anyString());
        doThrow(new RateLimitExceededException("Template rate limit exceeded"))
                .when(rateLimitingService).checkTemplateRateLimit(anyString(), anyString());

        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> notificationService.processNotification(requestDTO));
        assertEquals("Template rate limit exceeded", ex.getMessage());
        verify(notificationRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendNotification(any());
    }

    // --- Positive test: getNotificationById found ---
    @Test
    void getNotificationById_found() {
        Notification notif = new Notification();
        notif.setId("notif-42");
        when(notificationRepository.findById("notif-42")).thenReturn(Optional.of(notif));

        Optional<Notification> result = notificationService.getNotificationById("notif-42");
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("notif-42");
    }

    // --- Negative test: getNotificationById not found ---
    @Test
    void getNotificationById_notFound() {
        when(notificationRepository.findById("nope")).thenReturn(Optional.empty());
        Optional<Notification> result = notificationService.getNotificationById("nope");
        assertThat(result).isNotPresent();
    }

    // --- Positive test: getNotificationsByUserId ---
    @Test
    void getNotificationsByUserId() {
        List<Notification> notifs = Collections.singletonList(new Notification());
        when(notificationRepository.findByUserId("userX")).thenReturn(notifs);

        List<Notification> result = notificationService.getNotificationsByUserId("userX");
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    // --- Positive test: getNotificationsByCorrelationId ---
    @Test
    void getNotificationsByCorrelationId() {
        List<Notification> notifs = Collections.singletonList(new Notification());
        when(notificationRepository.findByCorrelationId("corrX")).thenReturn(notifs);

        List<Notification> result = notificationService.getNotificationsByCorrelationId("corrX");
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }
}