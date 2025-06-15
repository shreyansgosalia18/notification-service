package com.karboncard.assignment.notificationservice.exception;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRateLimitExceeded_withUserAndTemplateAndCount() {
        RateLimitExceededException ex = new RateLimitExceededException(
                "Rate exceeded", "user1", "tmpl1", 5, 3
        );
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> response = handler.handleRateLimitExceeded(ex, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Rate Limit Exceeded", body.get("error"));
        assertEquals("Rate exceeded", body.get("message"));
        assertEquals("user1", body.get("userId"));
        assertEquals("tmpl1", body.get("templateId"));
        assertEquals(5, body.get("currentCount"));
        assertEquals(3, body.get("maxAllowed"));
        assertTrue(body.containsKey("timestamp"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), body.get("status"));
    }

    @Test
    void handleRateLimitExceeded_withoutTemplateIdOrCount() {
        RateLimitExceededException ex = new RateLimitExceededException("Only user", "user2", null, 0, 0);
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> response = handler.handleRateLimitExceeded(ex, request);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("user2", body.get("userId"));
        assertFalse(body.containsKey("templateId"));
        assertFalse(body.containsKey("currentCount"));
        assertFalse(body.containsKey("maxAllowed"));
    }

    @Test
    void handleMethodArgumentNotValid_populatesValidationErrors() {
        // Mock FieldError
        FieldError err1 = new FieldError("obj", "field1", "must not be null");
        FieldError err2 = new FieldError("obj", "field2", "must not be empty");
        List<FieldError> fieldErrors = Arrays.asList(err1, err2);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getMessage()).thenReturn("validation failed");

        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex, null, HttpStatus.BAD_REQUEST, request);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Validation Error", body.get("error"));
        assertTrue(body.containsKey("errors"));

        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals("must not be null", errors.get("field1"));
        assertEquals("must not be empty", errors.get("field2"));
    }

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("bad argument");
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> response = handler.handleIllegalArgument(ex, request);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Invalid Argument", body.get("error"));
        assertEquals("bad argument", body.get("message"));
    }

    @Test
    void handleGeneral_returnsServerError() {
        Exception ex = new Exception("something failed");
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> response = handler.handleGeneral(ex, request);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred", body.get("message"));
    }
}