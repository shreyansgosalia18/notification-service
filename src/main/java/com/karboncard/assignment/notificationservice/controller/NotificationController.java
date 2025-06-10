package com.karboncard.assignment.notificationservice.controller;

import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;
import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.dto.response.NotificationResponseDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.service.NotificationService;
import com.karboncard.assignment.notificationservice.service.RateLimitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification API", description = "Endpoints for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final RateLimitingService rateLimitingService;

    @PostMapping
    @Operation(summary = "Send a notification", description = "Queues a notification for asynchronous delivery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Notification accepted for processing",
                    content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<NotificationResponseDTO> sendNotification(
            @Valid @RequestBody NotificationRequestDTO requestDTO) {

        log.info("Received notification request for userId: {}, type: {}, templateId: {}",
                requestDTO.getUserId(), requestDTO.getType(), requestDTO.getTemplateId());

        // Check rate limits
        try {
            rateLimitingService.checkUserRateLimit(requestDTO.getUserId());
        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for userId: {}, templateId: {}",
                    requestDTO.getUserId(), requestDTO.getTemplateId());

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(NotificationResponseDTO.builder()
                            .success(false)
                            .message("Rate limit exceeded. Try again later.")
                            .build());
        }

        // Generate unique notification ID if not provided
        String notificationId = requestDTO.getCorrelationId() != null ?
                requestDTO.getCorrelationId() : UUID.randomUUID().toString();

        // Process notification asynchronously
        notificationService.processNotification(requestDTO);

        // Return immediate response
        NotificationResponseDTO responseDTO = NotificationResponseDTO.builder()
                .id(notificationId)
                .success(true)
                .message("Notification queued successfully")
                .build();

        return ResponseEntity.accepted().body(responseDTO);
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification status", description = "Retrieve status of a notification by ID")
    public ResponseEntity<NotificationResponseDTO> getNotificationStatus(
            @PathVariable String notificationId) {

        log.info("Retrieving status for notification: {}", notificationId);

        Optional<Notification> notificationOpt = notificationService.getNotificationById(notificationId);

        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            NotificationResponseDTO responseDTO = NotificationResponseDTO.builder()
                    .id(notification.getId())
                    .success(true)
                    .message("Notification found")
                    .status(notification.getStatus())
                    .build();
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}