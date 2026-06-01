package com.v1rex.liftnexus.transportorder.exception;

import com.v1rex.liftnexus.common.exception.DomainException;
import com.v1rex.liftnexus.common.exception.ErrorCode;

public abstract sealed class TransportOrderDomainException extends DomainException
    permits TransportOrderNotFoundException, TransportOrderInvalidStateException {

  protected TransportOrderDomainException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
