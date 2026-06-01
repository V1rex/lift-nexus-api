package com.v1rex.liftnexus.transportorder.exception;

import com.v1rex.liftnexus.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransportOrderErrorCode implements ErrorCode {
  TRANSPORT_ORDER_NOT_FOUND(
      "transport_order_not_found", "Transport Order Not Found", HttpStatus.NOT_FOUND),

  TRANSPORT_ORDER_INVALID_STATE(
      "transport_order_invalid_state", "Transport Order Invalid State", HttpStatus.CONFLICT);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}
