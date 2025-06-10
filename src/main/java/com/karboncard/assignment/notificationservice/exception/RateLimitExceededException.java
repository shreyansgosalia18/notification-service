package com.karboncard.assignment.notificationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends Exception {

    private final String userId;
    private final String templateId;
    private final int currentCount;
    private final int maxAllowed;

    public RateLimitExceededException(String message) {
        super(message);
        this.userId = null;
        this.templateId = null;
        this.currentCount = 0;
        this.maxAllowed = 0;
    }

    public RateLimitExceededException(String message, String userId, String templateId, int currentCount, int maxAllowed) {
        super(message);
        this.userId = userId;
        this.templateId = templateId;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.userId = null;
        this.templateId = null;
        this.currentCount = 0;
        this.maxAllowed = 0;
    }

    public String getUserId() {
        return userId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }
}