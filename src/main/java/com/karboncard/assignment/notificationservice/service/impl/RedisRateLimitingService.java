package com.karboncard.assignment.notificationservice.service.impl;

import com.karboncard.assignment.notificationservice.config.RateLimitConfig;
import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;
import com.karboncard.assignment.notificationservice.service.RateLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisRateLimitingService implements RateLimitingService {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitingService.class);
    private static final String USER_RATE_KEY_PREFIX = "rate:user:";
    private static final String TEMPLATE_RATE_KEY_PREFIX = "rate:template:";

    private final RedisTemplate<String, Integer> redisTemplate;
    private final ValueOperations<String, Integer> valueOps;
    private final RateLimitConfig rateLimitConfig;

    @Autowired
    public RedisRateLimitingService(RedisTemplate<String, Integer> redisTemplate,
                                    RateLimitConfig rateLimitConfig) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public void checkUserRateLimit(String userId) throws RateLimitExceededException {
        String key = getUserRateKey(userId);
        Integer count = valueOps.get(key);

        if (count != null && count >= rateLimitConfig.getUserMaxRequests()) {
            log.warn("Rate limit exceeded for user: {}, count: {}, limit: {}",
                    userId, count, rateLimitConfig.getUserMaxRequests());
            throw new RateLimitExceededException(
                    String.format("User rate limit exceeded. Maximum %d notifications allowed per %d minutes",
                            rateLimitConfig.getUserMaxRequests(),
                            rateLimitConfig.getUserTimeWindowMinutes()));
        }

        log.debug("User {} has sent {} notifications in the current time window", userId, count);
    }

    @Override
    public void checkTemplateRateLimit(String userId, String templateId) throws RateLimitExceededException {
        String key = getTemplateRateKey(userId, templateId);
        Integer count = valueOps.get(key);

        if (count != null && count >= rateLimitConfig.getTemplateMaxRequests()) {
            log.warn("Template rate limit exceeded for user: {}, template: {}, count: {}, limit: {}",
                    userId, templateId, count, rateLimitConfig.getTemplateMaxRequests());
            throw new RateLimitExceededException(
                    String.format("Template rate limit exceeded. Maximum %d '%s' notifications allowed per %d minutes",
                            rateLimitConfig.getTemplateMaxRequests(),
                            templateId,
                            rateLimitConfig.getTemplateTimeWindowMinutes()));
        }

        log.debug("User {} has sent {} notifications for template {} in the current time window",
                userId, count, templateId);
    }

    @Override
    public void recordUserNotificationAttempt(String userId) {
        String key = getUserRateKey(userId);
        incrementOrCreate(key, rateLimitConfig.getUserTimeWindowMinutes());
    }

    @Override
    public void recordTemplateNotificationAttempt(String userId, String templateId) {
        String key = getTemplateRateKey(userId, templateId);
        incrementOrCreate(key, rateLimitConfig.getTemplateTimeWindowMinutes());
    }

    private void incrementOrCreate(String key, int expiryMinutes) {
        Boolean keyExists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(keyExists)) {
            valueOps.increment(key);
        } else {
            valueOps.set(key, 1, Duration.ofMinutes(expiryMinutes));
        }
    }

    private String getUserRateKey(String userId) {
        return USER_RATE_KEY_PREFIX + userId;
    }

    private String getTemplateRateKey(String userId, String templateId) {
        return TEMPLATE_RATE_KEY_PREFIX + userId + ":" + templateId;
    }
}