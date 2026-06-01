package com.v1rex.liftnexus.storagebin.exception;

public final class StorageBinCodeExistsException extends StorageBinDomainException {

  public StorageBinCodeExistsException(String binCode) {
    super(
        StorageBinErrorCode.STORAGE_BIN_CODE_EXISTS,
        "Storage bin with code '" + binCode + "' already exists.");
  }
}
