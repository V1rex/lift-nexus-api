package com.v1rex.liftnexus.planning.exception;

import com.v1rex.liftnexus.common.exception.DomainException;
import com.v1rex.liftnexus.common.exception.ErrorCode;

public abstract sealed class DispatchJobDomainException extends DomainException
    permits DispatchJobNotFoundException, DispatchJobInvalidStateException {

  protected DispatchJobDomainException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
