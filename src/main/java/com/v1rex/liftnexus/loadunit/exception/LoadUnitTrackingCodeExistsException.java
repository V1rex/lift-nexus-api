package com.v1rex.liftnexus.loadunit.exception;

public final class LoadUnitTrackingCodeExistsException extends LoadUnitDomainException {

  public LoadUnitTrackingCodeExistsException(String trackingCode) {
    super(
        LoadUnitErrorCode.LOAD_UNIT_TRACKING_CODE_EXISTS,
        "Load unit with tracking code '" + trackingCode + "' already exists.");
  }
}
