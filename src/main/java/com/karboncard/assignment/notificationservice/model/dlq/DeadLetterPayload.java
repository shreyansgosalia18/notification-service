package com.karboncard.assignment.notificationservice.model.dlq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadLetterPayload {
    private String originalTopic;
    private String originalKey;
    private String originalPayload;
    private String errorReason;
    private Instant timestamp;
}