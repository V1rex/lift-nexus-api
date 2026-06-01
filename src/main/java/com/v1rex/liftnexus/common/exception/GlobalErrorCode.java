package com.v1rex.liftnexus.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
  VALIDATION_FAILED("validation_failed", "Validation failed", HttpStatus.BAD_REQUEST),
  CONSTRAINT_VIOLATION("constraint_violation", "Validation failed", HttpStatus.BAD_REQUEST),
  TYPE_MISMATCH("type_mismatch", "Invalid parameter type", HttpStatus.BAD_REQUEST),
  MISSING_PARAMETER(
      "missing_required_parameter", "Missing required parameter", HttpStatus.BAD_REQUEST),
  PAYLOAD_TOO_LARGE("payload_too_large", "Payload too large", HttpStatus.PAYLOAD_TOO_LARGE),

  METHOD_NOT_ALLOWED(
      "method_not_allowed", "HTTP method not supported", HttpStatus.METHOD_NOT_ALLOWED),
  UNSUPPORTED_MEDIA_TYPE(
      "unsupported_media_type", "Content type not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
  MEDIA_TYPE_NOT_ACCEPTABLE(
      "media_type_not_acceptable", "Requested format not acceptable", HttpStatus.NOT_ACCEPTABLE),
  MALFORMED_REQUEST_BODY(
      "malformed_request_body", "Malformed request body", HttpStatus.BAD_REQUEST),
  SERIALIZATION_ERROR(
      "serialization_failed", "Response generation failed", HttpStatus.INTERNAL_SERVER_ERROR),

  DATABASE_CONFLICT("database_state_conflict", "Database state conflict", HttpStatus.CONFLICT),

  INTERNAL_SERVER_ERROR(
      "internal_server_error", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}
