package com.v1rex.liftnexus.forklift.exception;

import com.v1rex.liftnexus.common.exception.DomainException;
import com.v1rex.liftnexus.common.exception.ErrorCode;

public abstract sealed class ForkliftDomainException extends DomainException
    permits ForkliftNotFoundException {

  protected ForkliftDomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
  }
}