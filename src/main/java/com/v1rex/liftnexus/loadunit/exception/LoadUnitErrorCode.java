package com.v1rex.liftnexus.loadunit.exception;

import com.v1rex.liftnexus.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LoadUnitErrorCode implements ErrorCode {
  LOAD_UNIT_NOT_FOUND("load_unit_not_found", "Load Unit Not Found", HttpStatus.NOT_FOUND),

  LOAD_UNIT_TRACKING_CODE_EXISTS(
      "load_unit_tracking_code_already_exists",
      "Load Unit Tracking Code Already Exists",
      HttpStatus.CONFLICT);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}
