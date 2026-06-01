package com.v1rex.liftnexus.forklift.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operational state of a forklift")
public enum OperationalStatus {
  @Schema(description = "Forklift is operational and available for tasks")
  ACTIVE,
  @Schema(description = "Forklift is undergoing maintenance")
  MAINTENANCE,
  @Schema(description = "Forklift is currently charging")
  CHARGING,
  @Schema(description = "Forklift is offline or decommissioned")
  OFFLINE
}
