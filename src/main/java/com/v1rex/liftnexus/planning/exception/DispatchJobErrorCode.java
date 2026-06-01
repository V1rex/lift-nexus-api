package com.v1rex.liftnexus.planning.exception;

import com.v1rex.liftnexus.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DispatchJobErrorCode implements ErrorCode {
  DISPATCH_JOB_NOT_FOUND("dispatch_job_not_found", "Dispatch Job Not Found", HttpStatus.NOT_FOUND),

  DISPATCH_JOB_INVALID_STATE(
      "dispatch_job_invalid_state", "Dispatch Job Invalid State", HttpStatus.CONFLICT);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}
