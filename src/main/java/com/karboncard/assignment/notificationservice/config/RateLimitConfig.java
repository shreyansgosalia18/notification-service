package com.karboncard.assignment.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification.rate-limit")
public class RateLimitConfig {

    /**
     * Maximum number of notifications a single user can receive of any type within the time window
     */
    private int userMaxRequests = 5;

    /**
     * Time window in minutes for user-level rate limiting
     */
    private int userTimeWindowMinutes = 1;

    /**
     * Maximum number of notifications of a specific template a user can receive within the time window
     */
    private int templateMaxRequests = 2;

    /**
     * Time window in minutes for template-level rate limiting
     */
    private int templateTimeWindowMinutes = 5;

    public int getUserMaxRequests() {
        return userMaxRequests;
    }

    public void setUserMaxRequests(int userMaxRequests) {
        this.userMaxRequests = userMaxRequests;
    }

    public int getUserTimeWindowMinutes() {
        return userTimeWindowMinutes;
    }

    public void setUserTimeWindowMinutes(int userTimeWindowMinutes) {
        this.userTimeWindowMinutes = userTimeWindowMinutes;
    }

    public int getTemplateMaxRequests() {
        return templateMaxRequests;
    }

    public void setTemplateMaxRequests(int templateMaxRequests) {
        this.templateMaxRequests = templateMaxRequests;
    }

    public int getTemplateTimeWindowMinutes() {
        return templateTimeWindowMinutes;
    }

    public void setTemplateTimeWindowMinutes(int templateTimeWindowMinutes) {
        this.templateTimeWindowMinutes = templateTimeWindowMinutes;
    }
}