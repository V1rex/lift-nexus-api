package com.v1rex.liftnexus.common.exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {


        return createErrorResponse(ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {


        log.warn("Business rule violation at {}: {}",
                request.getRequestURI(),
                ex.getMessage());

        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
        ConstraintViolationException ex,
        HttpServletRequest request) {

    log.warn("Validation failed at {} : {}", request.getRequestURI(), ex.getMessage());


    String detailMessage = ex.getConstraintViolations().stream()
            .map(violation -> violation.getMessage())
            .findFirst()
            .orElse("Invalid request parameter.");

    return createErrorResponse(
            detailMessage,
            HttpStatus.BAD_REQUEST,
            request
    );
}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {


        log.error("Unhandled exception occurred at {} : ", request.getRequestURI(), ex);

        return createErrorResponse(
                "An unexpected error occurred.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));


        log.debug("Validation failed at {}: {}", request.getRequestURI(), errorMessage);

        return createErrorResponse("Validation Failed: " + errorMessage, HttpStatus.BAD_REQUEST, request);
    }


    private ResponseEntity<ApiError> createErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        ApiError error = new ApiError(
                message,
                status.value(),
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }
}


