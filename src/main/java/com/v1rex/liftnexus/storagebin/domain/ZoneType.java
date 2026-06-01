package com.v1rex.liftnexus.storagebin.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Functional zone classification of a storage bin location")
public enum ZoneType {
  @Schema(description = "General storage area")
  STORAGE,
  @Schema(description = "Inbound staging area for incoming goods")
  STAGING_IN,
  @Schema(description = "Outbound staging area for outgoing goods")
  STAGING_OUT,
  @Schema(description = "Battery charging station")
  CHARGING_STATION,
  @Schema(description = "Hazardous materials storage area")
  HAZMAT
}
