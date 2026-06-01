package com.v1rex.liftnexus.loadunit.exception;

import com.v1rex.liftnexus.common.exception.DomainException;
import com.v1rex.liftnexus.common.exception.ErrorCode;

public abstract sealed class LoadUnitDomainException extends DomainException
    permits LoadUnitNotFoundException, LoadUnitTrackingCodeExistsException {

  protected LoadUnitDomainException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
