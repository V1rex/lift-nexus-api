package com.v1rex.liftnexus.forklift.exception;

public final class ForkliftTypeNotFoundException extends ForkliftDomainException {

  public ForkliftTypeNotFoundException(Long id) {
    super(
        ForkliftErrorCode.FORKLIFT_TYPE_NOT_FOUND,
        "Forklift Type with ID " + id + " does not exist.");
  }
}
