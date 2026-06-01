package com.v1rex.liftnexus.storagebin.exception;

public final class StorageBinNotFoundException extends StorageBinDomainException {

  public StorageBinNotFoundException(Long id) {
    super(
        StorageBinErrorCode.STORAGE_BIN_NOT_FOUND,
        "Storage bin with ID " + id + " does not exist.");
  }
}
