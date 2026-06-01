package com.v1rex.liftnexus.loadunit.exception;

public final class LoadUnitNotFoundException extends LoadUnitDomainException {

  public LoadUnitNotFoundException(Long id) {
    super(LoadUnitErrorCode.LOAD_UNIT_NOT_FOUND, "Load unit with ID " + id + " does not exist.");
  }

  public LoadUnitNotFoundException(String trackingCode) {
    super(
        LoadUnitErrorCode.LOAD_UNIT_NOT_FOUND,
        "Load unit with tracking code '" + trackingCode + "' does not exist.");
  }
}
