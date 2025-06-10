package com.karboncard.assignment.notificationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NotificationServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Notification Service");
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
