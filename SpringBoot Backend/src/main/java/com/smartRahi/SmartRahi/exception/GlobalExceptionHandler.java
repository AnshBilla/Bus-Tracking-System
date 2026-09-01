// NEW FILE: exception/GlobalExceptionHandler.java
package com.smartRahi.SmartRahi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Catch your custom "Not Found" errors
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, String> body = Map.of(
                "message", ex.getMessage(),
                "path", request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Custom Bad Credentials Handler
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<?> badCredentialsException(Exception ex, WebRequest request) {
        Map<String, String> body = Map.of(
                "message", "Invalid username or password.",
                "path", request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // Custom ResponseStatusException Handler
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<?> responseStatusException(org.springframework.web.server.ResponseStatusException ex, WebRequest request) {
        Map<String, String> body = Map.of(
                "message", ex.getReason() != null ? ex.getReason() : "An error occurred.",
                "path", request.getDescription(false)
        );
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    // Catch all other generic errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest request) {
        Map<String, String> body = Map.of(
                "message", "An internal server error occurred.",
                "error", ex.getMessage() != null ? ex.getMessage() : "Unknown error",
                "path", request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}