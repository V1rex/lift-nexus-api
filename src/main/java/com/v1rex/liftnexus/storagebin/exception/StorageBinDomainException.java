package com.v1rex.liftnexus.storagebin.exception;

import com.v1rex.liftnexus.common.exception.DomainException;
import com.v1rex.liftnexus.common.exception.ErrorCode;

public abstract sealed class StorageBinDomainException extends DomainException
    permits StorageBinNotFoundException, StorageBinCodeExistsException {

  protected StorageBinDomainException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
