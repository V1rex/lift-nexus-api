package com.v1rex.liftnexus.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleResourceNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {

    log.warn("Resource not found at {}: {}",
            request.getRequestURI(),
            ex.getMessage());

    return createErrorResponse(
        HttpStatus.NOT_FOUND,
        "Resource not found",
        ex.getMessage(),
        request,
        "resource_not_found",
        List.of());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ProblemDetail> handleIllegalState(
      IllegalStateException ex, HttpServletRequest request) {

    log.warn("Business rule violation at {}: {}",
            request.getRequestURI(),
            ex.getMessage());

    return createErrorResponse(
        HttpStatus.CONFLICT, // Error 409
        "Business rule violation",
        ex.getMessage(),
        request,
        "business_rule_violation",
        List.of());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {

    log.warn("Invalid request at {}: {}",
            request.getRequestURI(),
            ex.getMessage());

    return createErrorResponse(
        HttpStatus.BAD_REQUEST, // Error 400 is returned
        "Invalid request",
        ex.getMessage(),
        request,
        "invalid_request",
        List.of());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {

    List<String> errors =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .sorted()
            .toList();

    log.warn("Validation failed at {}: {}",
            request.getRequestURI(),
            String.join("; ", errors));

    return createErrorResponse(
        HttpStatus.BAD_REQUEST, // Error 400 is returned
        "Validation failed",
        "One or more request parameters are invalid.",
        request,
        "constraint_violation",
        errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneralException(
      Exception ex, HttpServletRequest request) {

    log.error("Unhandled exception occurred at {} : ", request.getRequestURI(), ex);

    return createErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal server error",
        "An unexpected error occurred.",
        request,
        "internal_server_error",
        List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .filter(message -> !message.isBlank())
            .sorted()
            .collect(Collectors.joining(", "));

    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .filter(message -> !message.isBlank())
            .sorted()
            .toList();

    log.debug("Validation failed at {}: {}", request.getRequestURI(), errorMessage);

    return createErrorResponse(
        HttpStatus.BAD_REQUEST, // Error 400 is returned
        "Validation failed",
        errorMessage.isBlank() ? "One or more request fields are invalid." : errorMessage,
        request,
        "validation_failed",
        errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String message =
        "Parameter '"
            + ex.getName()
            + "' must be of type "
            + Objects.toString(ex.getRequiredType(), "unknown");

    log.warn("Type mismatch at {}: {}", request.getRequestURI(), message);

    return createErrorResponse(
        HttpStatus.BAD_REQUEST, // Error 400 is returned
        "Invalid parameter type",
        message,
        request,
        "type_mismatch",
        List.of());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleUnreadableMessage(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    log.warn("Unreadable request body at {}: {}", request.getRequestURI(), ex.getMessage());

    return createErrorResponse(
        HttpStatus.BAD_REQUEST, // Error 400 is returned
        "Malformed request body",
        "The request body could not be parsed.",
        request,
        "malformed_request_body",
        List.of());
  }

  private ResponseEntity<ProblemDetail> createErrorResponse(
      HttpStatus status,
      String title,
      String detail,
      HttpServletRequest request,
      String errorCode,
      List<String> errors) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setTitle(title);
    problemDetail.setType(URI.create("urn:liftnexus:problem:" + errorCode));
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", errorCode);
    problemDetail.setProperty("timestamp", Instant.now().toString());

    if (errors != null && !errors.isEmpty()) {
      problemDetail.setProperty("errors", errors);
    }

    return ResponseEntity.status(status).body(problemDetail);
  }
}
