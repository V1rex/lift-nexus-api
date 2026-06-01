package com.v1rex.liftnexus.transportorder.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a transport order")
public enum TransportOrderStatus {
  @Schema(description = "Order has been created and is awaiting assignment")
  OPEN,
  @Schema(description = "Order has been assigned to a forklift")
  ASSIGNED,
  @Schema(description = "Order is actively being executed")
  IN_PROGRESS,
  @Schema(description = "Order has been successfully completed")
  COMPLETED,
  @Schema(description = "Order execution failed")
  FAILED
}
