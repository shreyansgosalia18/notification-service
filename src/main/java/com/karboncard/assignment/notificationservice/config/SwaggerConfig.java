package com.karboncard.assignment.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${springdoc.server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("API for managing and sending notifications")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Karbon Card")
                                .url("https://www.karboncard.com")
                                .email("support@karboncard.com"))
                        .license(new License()
                                .name("API License")
                                .url("https://www.karboncard.com/license")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Server URL")
                ));
    }
}