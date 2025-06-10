package com.karboncard.assignment.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRetry
public class RetryConfig {

    @Value("${retry.max-attempts:5}")
    private int maxAttempts;

    @Value("${retry.initial-interval:1000}")
    private long initialInterval;

    @Value("${retry.multiplier:2.0}")
    private double multiplier;

    @Value("${retry.max-interval:60000}")
    private long maxInterval;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Configure retry policy
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        // Add retryable exceptions - typically transient errors
        retryableExceptions.put(RuntimeException.class, true);
        retryableExceptions.put(Exception.class, true);
        // Add non-retryable exceptions - typically permanent errors
        retryableExceptions.put(IllegalArgumentException.class, false);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}