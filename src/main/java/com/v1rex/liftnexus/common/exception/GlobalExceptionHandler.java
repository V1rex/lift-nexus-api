package com.v1rex.liftnexus.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE) // Resolves system framework-level failures as a fallback
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ProblemDetailFactory errorFactory;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .filter(msg -> !msg.isBlank())
            .sorted()
            .toList();

    log.debug("Payload validation failed at {}: {}", request.getRequestURI(), errors);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.VALIDATION_FAILED,
        "One or more request fields failed structural validation criteria.",
        request,
        errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {

    List<String> errors =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .sorted()
            .toList();

    log.warn("Parameter constraint violation at {}: {}", request.getRequestURI(), errors);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.CONSTRAINT_VIOLATION,
        "One or more request parameters are semantically invalid.",
        request,
        errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String message =
        String.format(
            "Parameter '%s' must be of data type %s.",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

    log.warn("Type mismatch triggered at {}: {}", request.getRequestURI(), message);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.TYPE_MISMATCH, message, request, List.of());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ProblemDetail> handleMissingParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {

    String message =
        String.format(
            "Required query parameter '%s' (%s) is missing.",
            ex.getParameterName(), ex.getParameterType());

    log.warn("Missing expected parameter at {}: {}", request.getRequestURI(), message);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.MISSING_PARAMETER, message, request, List.of());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceeded(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {

    log.warn("Payload size threshold breached at {}", request.getRequestURI());

    return errorFactory.createErrorResponse(
        GlobalErrorCode.PAYLOAD_TOO_LARGE,
        "The uploaded attachment size exceeds the configured max limit.",
        request,
        List.of());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

    String message =
        String.format(
            "HTTP verb '%s' is invalid for this route. Supported verbs: %s",
            ex.getMethod(), ex.getSupportedHttpMethods());

    log.warn("HTTP method mismatch at {}: {}", request.getRequestURI(), message);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.METHOD_NOT_ALLOWED, message, request, List.of());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ProblemDetail> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

    String message =
        String.format(
            "Content type '%s' is unacceptable. Supported formats: %s",
            ex.getContentType(), ex.getSupportedMediaTypes());

    log.warn("Unsupported incoming media type request at {}: {}", request.getRequestURI(), message);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE, message, request, List.of());
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ProblemDetail> handleMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {

    log.warn("Client Accept header negotiation failed at {}", request.getRequestURI());

    return errorFactory.createErrorResponse(
        GlobalErrorCode.MEDIA_TYPE_NOT_ACCEPTABLE,
        "Could not generate a response matching the format specified in the client Accept header.",
        request,
        List.of());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleUnreadableMessage(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    log.warn(
        "Unparseable payload stream detected at {}: {}", request.getRequestURI(), ex.getMessage());

    return errorFactory.createErrorResponse(
        GlobalErrorCode.MALFORMED_REQUEST_BODY,
        "The incoming JSON request body contains malformed syntax and cannot be parsed.",
        request,
        List.of());
  }

  @ExceptionHandler(HttpMessageNotWritableException.class)
  public ResponseEntity<ProblemDetail> handleWritableException(
      HttpMessageNotWritableException ex, HttpServletRequest request) {

    log.error("Outbound JSON serialization failed at {}", request.getRequestURI(), ex);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.SERIALIZATION_ERROR,
        "An error occurred while serializing the response payload.",
        request,
        List.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    String detail = "Database constraint violation.";
    // Try to extract constraint name
    String message = ex.getMessage();
    if (message != null && message.contains("Detail:")) {
      detail = message.substring(message.indexOf("Detail:"));
    }
    log.warn("Database constraint triggered at {}: {}", request.getRequestURI(), message);
    return errorFactory.createErrorResponse(
        GlobalErrorCode.DATABASE_CONFLICT, detail, request, List.of());
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ProblemDetail> handleDomainException(
      DomainException ex, HttpServletRequest request) {

    log.warn(
        "Unhandled domain exception [{}] at {}: {}",
        ex.getErrorCode().getCode(),
        request.getRequestURI(),
        ex.getMessage());

    return errorFactory.createErrorResponse(ex.getErrorCode(), ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneralException(
      Exception ex, HttpServletRequest request) {

    log.error("Critical unhandled system anomaly logged at {}: ", request.getRequestURI(), ex);

    return errorFactory.createErrorResponse(
        GlobalErrorCode.INTERNAL_SERVER_ERROR,
        "An unexpected software execution anomaly has occurred on the server.",
        request,
        List.of());
  }
}
