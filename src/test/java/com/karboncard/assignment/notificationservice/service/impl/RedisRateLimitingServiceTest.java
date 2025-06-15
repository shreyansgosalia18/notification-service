package com.karboncard.assignment.notificationservice.service.impl;

import com.karboncard.assignment.notificationservice.config.RateLimitConfig;
import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisRateLimitingServiceTest {

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;

    @Mock
    private ValueOperations<String, Integer> valueOps;

    @Mock
    private RateLimitConfig rateLimitConfig;

    private RedisRateLimitingService redisRateLimitingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        when(rateLimitConfig.getUserMaxRequests()).thenReturn(3);
        when(rateLimitConfig.getUserTimeWindowMinutes()).thenReturn(10);
        when(rateLimitConfig.getTemplateMaxRequests()).thenReturn(2);
        when(rateLimitConfig.getTemplateTimeWindowMinutes()).thenReturn(5);

        // MANUAL INSTANTIATION after stubs
        redisRateLimitingService = new RedisRateLimitingService(redisTemplate, rateLimitConfig);
    }

    @Test
    void checkUserRateLimit_firstRequest_setsExpiry_noException() {
        String userId = "user1";
        String key = "rate:user:" + userId;
        when(valueOps.increment(key)).thenReturn(1L);

        assertDoesNotThrow(() -> redisRateLimitingService.checkUserRateLimit(userId));
        verify(redisTemplate).expire(eq(key), eq(Duration.ofMinutes(10)));
    }

    @Test
    void checkUserRateLimit_withinLimit_noException() {
        String userId = "user1";
        String key = "rate:user:" + userId;
        when(valueOps.increment(key)).thenReturn(2L);

        assertDoesNotThrow(() -> redisRateLimitingService.checkUserRateLimit(userId));
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    @Test
    void checkUserRateLimit_exceedsLimit_throwsException() {
        String userId = "user1";
        String key = "rate:user:" + userId;
        when(valueOps.increment(key)).thenReturn(4L);

        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> redisRateLimitingService.checkUserRateLimit(userId));
        assertTrue(ex.getMessage().contains("User rate limit exceeded"));
        assertEquals(userId, ex.getUserId());
        assertNull(ex.getTemplateId());
        assertEquals(4, ex.getCurrentCount());
        assertEquals(3, ex.getMaxAllowed());
    }

    @Test
    void checkTemplateRateLimit_firstRequest_setsExpiry_noException() {
        String userId = "user1";
        String templateId = "tmpl";
        String key = "rate:template:" + userId + ":" + templateId;
        when(valueOps.increment(key)).thenReturn(1L);

        assertDoesNotThrow(() -> redisRateLimitingService.checkTemplateRateLimit(userId, templateId));
        verify(redisTemplate).expire(eq(key), eq(Duration.ofMinutes(5)));
    }

    @Test
    void checkTemplateRateLimit_withinLimit_noException() {
        String userId = "user1";
        String templateId = "tmpl";
        String key = "rate:template:" + userId + ":" + templateId;
        when(valueOps.increment(key)).thenReturn(2L);

        assertDoesNotThrow(() -> redisRateLimitingService.checkTemplateRateLimit(userId, templateId));
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    @Test
    void checkTemplateRateLimit_exceedsLimit_throwsException() {
        String userId = "user1";
        String templateId = "tmpl";
        String key = "rate:template:" + userId + ":" + templateId;
        when(valueOps.increment(key)).thenReturn(3L);

        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> redisRateLimitingService.checkTemplateRateLimit(userId, templateId));
        assertTrue(ex.getMessage().contains("Template rate limit exceeded"));
        assertEquals(userId, ex.getUserId());
        assertEquals(templateId, ex.getTemplateId());
        assertEquals(3, ex.getCurrentCount());
        assertEquals(2, ex.getMaxAllowed());
    }

    @Test
    void recordUserNotificationAttempt_noop() {
        assertDoesNotThrow(() -> redisRateLimitingService.recordUserNotificationAttempt("user1"));
    }

    @Test
    void recordTemplateNotificationAttempt_noop() {
        assertDoesNotThrow(() -> redisRateLimitingService.recordTemplateNotificationAttempt("user1", "tmpl"));
    }
}