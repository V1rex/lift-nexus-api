package com.v1rex.liftnexus.forklift.exception;

import com.v1rex.liftnexus.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ForkliftErrorCode implements ErrorCode {

  FORKLIFT_NOT_FOUND("forklift_not_found",
                                "Forklift Not Found",
                                                    HttpStatus.NOT_FOUND);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}