package com.karboncard.assignment.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karboncard.assignment.notificationservice.exception.RateLimitExceededException;
import com.karboncard.assignment.notificationservice.model.dto.request.NotificationRequestDTO;
import com.karboncard.assignment.notificationservice.model.dto.response.NotificationResponseDTO;
import com.karboncard.assignment.notificationservice.model.entity.Notification;
import com.karboncard.assignment.notificationservice.model.enums.NotificationStatus;
import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import com.karboncard.assignment.notificationservice.service.NotificationService;
import com.karboncard.assignment.notificationservice.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RateLimitingService rateLimitingService;

    @InjectMocks
    private NotificationController notificationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
    }

    // --------- POSITIVE CASES ----------

    @Test
    void sendNotification_email_success() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "user@example.com");

        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u1")
                .type(NotificationType.EMAIL)
                .templateId("TEMPLATE1")
                .templateParams(params)
                .build();

        NotificationResponseDTO response = NotificationResponseDTO.builder()
                .id("notif-123")
                .success(true)
                .message("Notification queued")
                .build();

        when(notificationService.processNotification(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is("notif-123")))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Notification queued")));
    }

    @Test
    void sendNotification_sms_success() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("phoneNumber", "9999999999");

        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u2")
                .type(NotificationType.SMS)
                .templateId("TEMPLATE2")
                .templateParams(params)
                .build();

        NotificationResponseDTO response = NotificationResponseDTO.builder()
                .id("notif-456")
                .success(true)
                .message("Notification queued")
                .build();

        when(notificationService.processNotification(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is("notif-456")))
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void getNotificationStatus_found() throws Exception {
        Notification notification = new Notification();
        notification.setId("notif-789");
        notification.setStatus(NotificationStatus.SENT);

        when(notificationService.getNotificationById("notif-789")).thenReturn(Optional.of(notification));

        mockMvc.perform(get("/api/v1/notifications/notif-789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("notif-789")))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("SENT")));
    }

    // --------- NEGATIVE CASES ----------

    @Test
    void sendNotification_missingType() throws Exception {
        // type=null, should fail bean validation and not hit controller, so default error body
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u1")
                .templateId("TEMPLATE1")
                .templateParams(Map.of("email", "user@example.com"))
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_missingTemplateParams() throws Exception {
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u1")
                .type(NotificationType.EMAIL)
                .templateId("TEMPLATE1")
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Missing 'templateParams' in request.")));
    }

    @Test
    void sendNotification_missingEmailForEmailType() throws Exception {
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u1")
                .type(NotificationType.EMAIL)
                .templateId("TEMPLATE1")
                .templateParams(Map.of())
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Missing required field 'email' for EMAIL notification in template_params.")));
    }

    @Test
    void sendNotification_rateLimitExceeded() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "user@example.com");

        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u1")
                .type(NotificationType.EMAIL)
                .templateId("TEMPLATE1")
                .templateParams(params)
                .build();

        when(notificationService.processNotification(any()))
                .thenThrow(new RateLimitExceededException("Rate limit exceeded for user"));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Rate limit exceeded for user")));
    }

    @Test
    void sendNotification_duplicateRequest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "user@example.com");

        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u1")
                .type(NotificationType.EMAIL)
                .templateId("TEMPLATE1")
                .templateParams(params)
                .build();

        NotificationResponseDTO responseDTO = NotificationResponseDTO.builder()
                .id("notif-dup")
                .success(false)
                .message("Duplicate request")
                .build();

        when(notificationService.processNotification(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Duplicate request")));
    }

    @Test
    void sendNotification_unknownType() throws Exception {
        // type=null, should fail bean validation and not hit controller, so default error body
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId("u1")
                .type(null)
                .templateId("TEMPLATE1")
                .templateParams(Map.of("foo", "bar"))
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotificationStatus_notFound() throws Exception {
        when(notificationService.getNotificationById("missing-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/notifications/missing-id"))
                .andExpect(status().isNotFound());
    }
}