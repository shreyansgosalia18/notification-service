package com.karboncard.assignment.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karboncard.assignment.notificationservice.model.dlq.DeadLetterPayload;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class DeadLetterQueueServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DeadLetterQueueService deadLetterQueueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set the private field deadLetterTopic value using reflection since it is injected via @Value
        try {
            java.lang.reflect.Field f = DeadLetterQueueService.class.getDeclaredField("deadLetterTopic");
            f.setAccessible(true);
            f.set(deadLetterQueueService, "test-dlq-topic");
        } catch (Exception ignored) {}
    }

    @Test
    void sendToDLQ_success() throws Exception {
        DeadLetterPayload payload = new DeadLetterPayload();
        payload.setOriginalKey("key1");
        String payloadJson = "{\"originalKey\":\"key1\"}";

        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);

        deadLetterQueueService.sendToDLQ(payload);

        verify(objectMapper).writeValueAsString(payload);
        verify(kafkaTemplate).send("test-dlq-topic", "key1", payloadJson);
    }

    @Test
    void sendToDLQ_jsonProcessingException() throws Exception {
        DeadLetterPayload payload = new DeadLetterPayload();
        payload.setOriginalKey("key2");

        when(objectMapper.writeValueAsString(payload)).thenThrow(new JsonProcessingException("fail") {});

        // Should not throw but should log error
        deadLetterQueueService.sendToDLQ(payload);

        verify(objectMapper).writeValueAsString(payload);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void sendToDLQ_kafkaSendException() throws Exception {
        DeadLetterPayload payload = new DeadLetterPayload();
        payload.setOriginalKey("key3");
        String payloadJson = "{\"originalKey\":\"key3\"}";

        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);
        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaTemplate).send(anyString(), anyString(), anyString());

        // Should not throw but should log error
        deadLetterQueueService.sendToDLQ(payload);

        verify(objectMapper).writeValueAsString(payload);
        verify(kafkaTemplate).send("test-dlq-topic", "key3", payloadJson);
    }
}