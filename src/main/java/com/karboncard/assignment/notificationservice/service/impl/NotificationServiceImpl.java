package com.karboncard.assignment.notificationservice.service.impl;

import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;
import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.dto.response.NotificationResponseDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.repository.NotificationRepository;
import com.karboncard.assignment.notificationservice.service.KafkaProducerService;
import com.karboncard.assignment.notificationservice.service.NotificationService;
import com.karboncard.assignment.notificationservice.service.RateLimitingService;
import com.karboncard.assignment.notificationservice.util.IdempotencyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RateLimitingService rateLimitingService;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   KafkaProducerService kafkaProducerService,
                                   RateLimitingService rateLimitingService) {
        this.notificationRepository = notificationRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    @Transactional
    public NotificationResponseDTO processNotification(NotificationRequestDTO requestDTO) {
        log.info("Processing notification request for user: {}, type: {}",
                requestDTO.getUserId(), requestDTO.getType());

        // Generate idempotency key if not provided
        if (requestDTO.getIdempotencyKey() == null || requestDTO.getIdempotencyKey().isEmpty()) {
            String generatedKey = generateIdempotencyKey(requestDTO);
            log.info("Generated idempotency key: {}", generatedKey);
            requestDTO.setIdempotencyKey(generatedKey);
        }

        // Check idempotency key
        Optional<Notification> existingNotification =
                notificationRepository.findByIdempotencyKey(requestDTO.getIdempotencyKey());
        if (existingNotification.isPresent()) {
            log.info("Duplicate request with idempotency key: {}", requestDTO.getIdempotencyKey());
            return buildResponseDTO(existingNotification.get(), "Duplicate request");
        }

        // Apply rate limits
        try {
            // Check rate limit per user
            rateLimitingService.checkUserRateLimit(requestDTO.getUserId());

            // Check rate limit per template type for this user
            rateLimitingService.checkTemplateRateLimit(requestDTO.getUserId(), requestDTO.getTemplateId());
        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded: {}", e.getMessage());
            return NotificationResponseDTO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }

        // Convert DTO to entity
        Notification notification = convertToEntity(requestDTO);

        // Save to database
        notification = notificationRepository.save(notification);
        log.info("Notification saved with ID: {}", notification.getId());

        // Send to Kafka for asynchronous processing
        kafkaProducerService.sendNotification(notification);
        log.info("Notification sent to Kafka for processing");

        // Return response
        return buildResponseDTO(notification, "Notification queued successfully");
    }

    /**
     * Generates a unique idempotency key based on request properties
     */
    private String generateIdempotencyKey(NotificationRequestDTO requestDTO) {
        return String.format("%s-%s-%s-%s-%s",
                requestDTO.getType().toString().toLowerCase(),
                requestDTO.getTemplateId().toLowerCase(),
                requestDTO.getUserId(),
                requestDTO.getCorrelationId(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    @Override
    public Optional<Notification> getNotificationById(String id) {
        return notificationRepository.findById(id);
    }

    @Override
    public List<Notification> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> getNotificationsByCorrelationId(String correlationId) {
        return notificationRepository.findByCorrelationId(correlationId);
    }

    private Notification convertToEntity(NotificationRequestDTO dto) {
        Notification notification = new Notification();
//        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(dto.getUserId());
        notification.setType(dto.getType());
        notification.setTemplateId(dto.getTemplateId());
        notification.setTemplateParams(dto.getTemplateParams());
        notification.setPriority(dto.getPriority());
        notification.setCorrelationId(dto.getCorrelationId());
        notification.setIdempotencyKey(dto.getIdempotencyKey());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notification.setDeliveryAttempts(0);
        return notification;
    }

    private NotificationResponseDTO buildResponseDTO(Notification notification, String message) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .success(true)
                .message(message)
                .build();
    }
}