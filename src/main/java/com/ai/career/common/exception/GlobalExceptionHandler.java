package com.ai.career.common.exception;

import com.ai.career.common.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
            .error(ex.getMessage())
            .code("BAD_REQUEST")
            .status(HttpStatus.BAD_REQUEST.value())
            .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ApiErrorResponse response = ApiErrorResponse.builder()
            .error("Invalid email or password")
            .code("UNAUTHORIZED")
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());

        ApiErrorResponse response = ApiErrorResponse.builder()
            .error("Validation failed for input arguments")
            .code("VALIDATION_ERROR")
            .status(HttpStatus.BAD_REQUEST.value())
            .details(errors)
            .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception caught by global handler: ", ex);
        ApiErrorResponse response = ApiErrorResponse.builder()
            .error("An internal server error occurred")
            .code("INTERNAL_SERVER_ERROR")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
