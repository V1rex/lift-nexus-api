package com.v1rex.liftnexus.common.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

  private final ErrorCode errorCode;

  protected DomainException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
