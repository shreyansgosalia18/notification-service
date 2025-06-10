package com.karboncard.assignment.notificationservice.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for recording metrics about the notification service.
 * Uses Micrometer for instrumentation which can be connected to various
 * monitoring systems like Prometheus, Datadog, etc.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsUtil {

    private final MeterRegistry meterRegistry;

    /**
     * Increments a counter metric with tags
     *
     * @param name The metric name
     * @param tags A map of tag names and values
     */
    public void incrementCounter(String name, Map<String, String> tags) {
        meterRegistry.counter(name, convertToTags(tags)).increment();
    }

    /**
     * Records a latency measurement
     *
     * @param name The metric name
     * @param timeMs The time in milliseconds
     * @param tags Optional tags for the metric
     */
    public void recordLatency(String name, long timeMs, Map<String, String> tags) {
        meterRegistry.timer(name, convertToTags(tags)).record(timeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Records a latency measurement without tags
     *
     * @param name The metric name
     * @param timeMs The time in milliseconds
     */
    public void recordLatency(String name, long timeMs) {
        recordLatency(name, timeMs, Map.of());
    }

    /**
     * Records a gauge value
     *
     * @param name The metric name
     * @param value The value to record
     * @param tags Optional tags for the metric
     */
    public void recordGauge(String name, double value, Map<String, String> tags) {
        meterRegistry.gauge(name, convertToTags(tags), value);
    }

    /**
     * Converts a map of tag key-values to a list of Tag objects for Micrometer
     */
    private Iterable<Tag> convertToTags(Map<String, String> tagMap) {
        List<Tag> tags = new ArrayList<>();
        if (tagMap != null) {
            tagMap.forEach((key, value) -> tags.add(Tag.of(key, value)));
        }
        return tags;
    }

    /**
     * Record notification event metrics
     *
     * @param type Notification type (SMS, EMAIL, PUSH)
     * @param status Status of the notification (SUCCESS, FAILURE)
     */
    public void recordNotificationEvent(String type, String status) {
        incrementCounter("notification.events",
                Map.of("type", type, "status", status));
    }

    /**
     * Record rate limiting metrics
     *
     * @param type The type of rate limit (USER, TEMPLATE)
     * @param allowed Whether the request was allowed or rejected
     */
    public void recordRateLimitEvent(String type, boolean allowed) {
        incrementCounter("notification.ratelimit",
                Map.of("type", type, "allowed", allowed ? "true" : "false"));
    }
}