package com.sam.finance.sahamlog.shared.api;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sam.finance.sahamlog.shared.exception.BusinessRuleViolationException;
import com.sam.finance.sahamlog.shared.exception.ConflictException;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(
        MethodArgumentNotValidException exception,
        HttpServletRequest request) {

        List<String> details = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .toList();

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            request.getRequestURI(),
            details);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(
        ResourceNotFoundException exception,
        HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI(),
            List.of());
    }

    @ExceptionHandler({ConflictException.class, BusinessRuleViolationException.class})
    ResponseEntity<ApiErrorResponse> handleConflictLikeErrors(
        RuntimeException exception,
        HttpServletRequest request) {

        HttpStatus status = exception instanceof ConflictException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return buildResponse(status, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleBadCredentials(
        BadCredentialsException exception,
        HttpServletRequest request) {

        return buildResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password",
            request.getRequestURI(),
            List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleGenericException(
        Exception exception,
        HttpServletRequest request) {

        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getMessage(),
            request.getRequestURI(),
            List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
        HttpStatus status,
        String message,
        String path,
        List<String> details) {

        return ResponseEntity.status(status)
            .body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details));
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
