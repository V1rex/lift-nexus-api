package com.v1rex.liftnexus.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
  CONSTRAINT_VIOLATION("constraint_violation", "Validation failed", HttpStatus.BAD_REQUEST),

  VALIDATION_FAILED("validation_failed", "Validation failed", HttpStatus.BAD_REQUEST),
  TYPE_MISMATCH("type_mismatch", "Invalid parameter type", HttpStatus.BAD_REQUEST),

  MALFORMED_REQUEST_BODY(
      "malformed_request_body", "Malformed request body", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR(
      "internal_server_error", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}
