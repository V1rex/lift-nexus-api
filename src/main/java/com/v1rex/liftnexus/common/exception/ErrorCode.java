package com.v1rex.liftnexus.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  String getCode();

  String getDefaultTitle();

  HttpStatus getStatus();
}
