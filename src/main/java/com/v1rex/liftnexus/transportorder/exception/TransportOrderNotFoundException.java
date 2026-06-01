package com.v1rex.liftnexus.transportorder.exception;

public final class TransportOrderNotFoundException extends TransportOrderDomainException {

  public TransportOrderNotFoundException(Long id) {
    super(
        TransportOrderErrorCode.TRANSPORT_ORDER_NOT_FOUND,
        "Transport order with ID " + id + " does not exist.");
  }
}
