package com.v1rex.liftnexus.forklift.exception;

public final class ForkliftFleetNumberExistsException extends ForkliftDomainException {

  public ForkliftFleetNumberExistsException(String fleetNumber) {
    super(
        ForkliftErrorCode.FORKLIFT_FLEET_NUMBER_EXISTS,
        "A forklift type with fleet number'" + fleetNumber + "' already exists.");
  }
}
