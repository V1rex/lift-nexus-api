package com.v1rex.liftnexus.planning.exception;

public final class DispatchJobInvalidStateException extends DispatchJobDomainException {

  public DispatchJobInvalidStateException(String message) {
    super(DispatchJobErrorCode.DISPATCH_JOB_INVALID_STATE, message);
  }
}
