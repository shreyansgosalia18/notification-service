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

    // Atomic user rate limit: increments first, checks after
    @Override
    public void checkUserRateLimit(String userId) throws RateLimitExceededException {
        String key = getUserRateKey(userId);
        Long count = valueOps.increment(key);
        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(rateLimitConfig.getUserTimeWindowMinutes()));
        }
        if (count > rateLimitConfig.getUserMaxRequests()) {
            log.warn("User rate limit exceeded for user: {}, count: {}, limit: {}",
                    userId, count, rateLimitConfig.getUserMaxRequests());
            throw new RateLimitExceededException(
                    String.format("User rate limit exceeded. Maximum %d notifications allowed per %d minutes",
                            rateLimitConfig.getUserMaxRequests(),
                            rateLimitConfig.getUserTimeWindowMinutes()),
                    userId, null, count.intValue(), rateLimitConfig.getUserMaxRequests()
            );
        }
        log.debug("User {} has sent {} notifications in the current time window", userId, count);
    }

    // Atomic template rate limit: increments first, checks after
    @Override
    public void checkTemplateRateLimit(String userId, String templateId) throws RateLimitExceededException {
        String key = getTemplateRateKey(userId, templateId);
        Long count = valueOps.increment(key);
        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(rateLimitConfig.getTemplateTimeWindowMinutes()));
        }
        if (count > rateLimitConfig.getTemplateMaxRequests()) {
            log.warn("Template rate limit exceeded for user: {}, template: {}, count: {}, limit: {}",
                    userId, templateId, count, rateLimitConfig.getTemplateMaxRequests());
            throw new RateLimitExceededException(
                    String.format("Template rate limit exceeded. Maximum %d '%s' notifications allowed per %d minutes",
                            rateLimitConfig.getTemplateMaxRequests(),
                            templateId,
                            rateLimitConfig.getTemplateTimeWindowMinutes()),
                    userId, templateId, count.intValue(), rateLimitConfig.getTemplateMaxRequests()
            );
        }
        log.debug("User {} has sent {} notifications for template {} in the current time window",
                userId, count, templateId);
    }

    // These methods are now NO-OPs, since atomic check+increment is in the above methods
    @Override
    public void recordUserNotificationAttempt(String userId) {
        // No longer needed
    }

    @Override
    public void recordTemplateNotificationAttempt(String userId, String templateId) {
        // No longer needed
    }

    private String getUserRateKey(String userId) {
        return USER_RATE_KEY_PREFIX + userId;
    }

    private String getTemplateRateKey(String userId, String templateId) {
        return TEMPLATE_RATE_KEY_PREFIX + userId + ":" + templateId;
    }
}