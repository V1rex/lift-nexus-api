package com.v1rex.liftnexus.forklift.exception;

public final class ForkliftNotFoundException extends ForkliftDomainException {

  public ForkliftNotFoundException(Long id) {
    super(ForkliftErrorCode.FORKLIFT_NOT_FOUND, "Forklift with ID " + id + " does not exist.");
  }
}
