package com.karboncard.assignment.notificationservice.model.dto.response;

import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private String id;
    private String userId;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private boolean success;
    private String message;
}