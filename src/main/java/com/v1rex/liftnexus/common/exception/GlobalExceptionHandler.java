package com.v1rex.liftnexus.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ProblemDetailFactory errorFactory;

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {

    List<String> errors =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .sorted()
            .toList();

    log.warn("Validation failed at {}: {}", request.getRequestURI(), String.join("; ", errors));

    return errorFactory.createErrorResponse(
        GlobalErrorCode.CONSTRAINT_VIOLATION,
        "One or more request parameters are invalid.",
        request,
        errors);
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

    return errorFactory.createErrorResponse(
        GlobalErrorCode.VALIDATION_FAILED,
        errorMessage.isBlank() ? "One or more request fields are invalid." : errorMessage,
        request,
        errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String message =
        "Parameter '"
            + ex.getName()
            + "' must be of type "
            + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

    log.warn("Type mismatch at {}: {}", request.getRequestURI(), message);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.TYPE_MISMATCH, message, request, List.of());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleUnreadableMessage(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    log.warn("Unreadable request body at {}: {}", request.getRequestURI(), ex.getMessage());

    return errorFactory.createErrorResponse(
        GlobalErrorCode.MALFORMED_REQUEST_BODY,
        "The request body could not be parsed.",
        request,
        List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneralException(
      Exception ex, HttpServletRequest request) {

    log.error("Unhandled exception occurred at {} : ", request.getRequestURI(), ex);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request, List.of());
  }
}
