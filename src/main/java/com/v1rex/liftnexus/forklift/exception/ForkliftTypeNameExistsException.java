package com.v1rex.liftnexus.forklift.exception;

public final class ForkliftTypeNameExistsException extends ForkliftDomainException {

  public ForkliftTypeNameExistsException(String modelName) {
    super(
        ForkliftErrorCode.FORKLIFT_TYPE_NAME_EXISTS,
        "A forklift type model named '" + modelName + "' already exists.");
  }
}
