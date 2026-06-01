package com.v1rex.liftnexus.transportorder.exception;

public final class TransportOrderInvalidStateException extends TransportOrderDomainException {

  public TransportOrderInvalidStateException(String message) {
    super(TransportOrderErrorCode.TRANSPORT_ORDER_INVALID_STATE, message);
  }
}
