package com.grim.contextos.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void businessExceptionStoresCodeAndMessage() {
        var ex = new BusinessException("TEST_CODE", "Test message");
        assertEquals("TEST_CODE", ex.getCode());
        assertEquals("Test message", ex.getMessage());
    }

    @Test
    void resourceNotFoundExceptionFormatsMessage() {
        var ex = new ResourceNotFoundException("User", 42);
        assertEquals("NOT_FOUND", ex.getCode());
        assertEquals("User not found with id: 42", ex.getMessage());
    }

    @Test
    void resourceNotFoundExceptionWithUuid() {
        var ex = new ResourceNotFoundException("Order", java.util.UUID.randomUUID());
        assertEquals("NOT_FOUND", ex.getCode());
        assertTrue(ex.getMessage().startsWith("Order not found with id: "));
    }

    @Test
    void validationExceptionStoresFields() {
        var fieldError = new ValidationException.FieldError("email", "must not be blank");
        var ex = new ValidationException("Validation failed", java.util.List.of(fieldError));

        assertEquals("VALIDATION_ERROR", ex.getCode());
        assertEquals("Validation failed", ex.getMessage());
        assertEquals(1, ex.getFieldErrors().size());
        assertEquals("email", ex.getFieldErrors().getFirst().field());
        assertEquals("must not be blank", ex.getFieldErrors().getFirst().message());
    }

    @Test
    void validationExceptionWithMultipleFields() {
        var errors = java.util.List.of(
            new ValidationException.FieldError("email", "must not be blank"),
            new ValidationException.FieldError("password", "size must be between 8 and 100")
        );
        var ex = new ValidationException("Validation failed", errors);

        assertEquals(2, ex.getFieldErrors().size());
    }

    @Test
    void unauthorizedExceptionStoresCode() {
        var ex = new UnauthorizedException("Invalid token");
        assertEquals("UNAUTHORIZED", ex.getCode());
        assertEquals("Invalid token", ex.getMessage());
    }

    @Test
    void forbiddenExceptionStoresCode() {
        var ex = new ForbiddenException("Access denied");
        assertEquals("FORBIDDEN", ex.getCode());
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void businessExceptionIsRuntimeException() {
        var ex = new BusinessException("CODE", "msg");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
