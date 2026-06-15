package com.grim.contextos.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundReturns404() {
        var ex = new ResourceNotFoundException("User", 1);
        ResponseEntity<Map<String, Object>> resp = handler.handleNotFound(ex);

        assertEquals(404, resp.getStatusCode().value());
        assertEquals("NOT_FOUND", ((Map) resp.getBody().get("error")).get("code"));
    }

    @Test
    void handleUnauthorizedReturns401() {
        var ex = new UnauthorizedException("Invalid token");
        ResponseEntity<Map<String, Object>> resp = handler.handleUnauthorized(ex);

        assertEquals(401, resp.getStatusCode().value());
        assertEquals("UNAUTHORIZED", ((Map) resp.getBody().get("error")).get("code"));
    }

    @Test
    void handleForbiddenReturns403() {
        var ex = new ForbiddenException("Access denied");
        ResponseEntity<Map<String, Object>> resp = handler.handleForbidden(ex);

        assertEquals(403, resp.getStatusCode().value());
        assertEquals("FORBIDDEN", ((Map) resp.getBody().get("error")).get("code"));
    }

    @Test
    void handleBadCredentialsReturns401() {
        var ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<Map<String, Object>> resp = handler.handleBadCredentials(ex);

        assertEquals(401, resp.getStatusCode().value());
        Map error = (Map) resp.getBody().get("error");
        assertEquals("INVALID_CREDENTIALS", error.get("code"));
        assertEquals("Bad credentials", error.get("message"));
    }

    @Test
    void handleRuntimeReturnsConflictForEmailExists() {
        var ex = new RuntimeException("Email already registered");
        ResponseEntity<Map<String, Object>> resp = handler.handleRuntime(ex);

        assertEquals(409, resp.getStatusCode().value());
        assertEquals("EMAIL_EXISTS", ((Map) resp.getBody().get("error")).get("code"));
    }

    @Test
    void handleRuntimeReturnsBadRequestForOther() {
        var ex = new RuntimeException("Something broke");
        ResponseEntity<Map<String, Object>> resp = handler.handleRuntime(ex);

        assertEquals(400, resp.getStatusCode().value());
        assertEquals("BAD_REQUEST", ((Map) resp.getBody().get("error")).get("code"));
    }

    @Test
    void handleRuntimeReturnsBadRequestForNullMessage() {
        var ex = new RuntimeException((String) null);
        ResponseEntity<Map<String, Object>> resp = handler.handleRuntime(ex);

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void handleGenericExceptionReturns500() {
        var ex = new Exception("Unexpected error");
        ResponseEntity<Map<String, Object>> resp = handler.handleException(ex);

        assertEquals(500, resp.getStatusCode().value());
        assertEquals("INTERNAL_SERVER_ERROR", ((Map) resp.getBody().get("error")).get("code"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void handleValidationReturns400() {
        var ex = new ValidationException("Validation failed", java.util.List.of(
            new ValidationException.FieldError("email", "must not be blank")
        ));
        ResponseEntity<Map<String, Object>> resp = handler.handleValidation((ValidationException) ex);

        assertEquals(400, resp.getStatusCode().value());
        var error = (Map<String, Object>) resp.getBody().get("error");
        assertEquals("VALIDATION_ERROR", error.get("code"));
    }
}
