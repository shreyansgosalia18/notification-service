package com.karboncard.assignment.notificationservice.model.dto.request;

import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Template ID is required")
    private String templateId;

    private Map<String, Object> templateParams;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    private String correlationId;

    @Schema(description = "Optional idempotency key for duplicate detection")
    private String idempotencyKey;
}