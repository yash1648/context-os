package com.grim.contextos.common.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(
            ResourceNotFoundException ex
    ){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getCode(),ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(ValidationException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of("success",false,"error",Map.of("code",ex.getCode(),
                                "message",ex.getMessage(),"details",ex.getFieldErrors(),"timestamp",
                                Instant.now().toString()))
                );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String,Object>> handleUnauthorized(UnauthorizedException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(ex.getCode(),ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String,Object>> handleForbidden(ForbiddenException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(ex.getCode(),ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String,Object>> handleBadCredentials(BadCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody("INVALID_CREDENTIALS", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,Object>> handleRuntime(RuntimeException ex){
        if (ex.getMessage() != null && ex.getMessage().contains("Email already")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(errorBody("EMAIL_EXISTS", ex.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex){
        var errors=ex.getBindingResult().getFieldErrors().stream()
                .map(fe->Map.of("field",fe.getField(),"message",fe.getDefaultMessage())).toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of("success",false,
                                "error",Map.of("code","VALIDATION_ERROR","message","Validation failed","details",errors)
                        ,"timestamp",Instant.now().toString())
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleException(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_SERVER_ERROR",ex.getMessage()));
    }

    private Map<String,Object> errorBody(String code,String message){

        return Map.of(
                "success",false,
                "error",Map.of("code",code,"message",message,"details", List.of()),
                "timestamp",Instant.now().toString()
        );

    }

}
