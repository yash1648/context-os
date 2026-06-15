package com.grim.contextos.common.exception;

import org.springframework.validation.FieldError;

import java.util.List;

public class ValidationException extends BusinessException{
    public final List<FieldError> fieldErrors;
    public ValidationException(String message,List<FieldError> fieldErrors) {
        super("VALIDATION_ERROR",message);
        this.fieldErrors = fieldErrors;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public record FieldError(String field, String message) {}

}
