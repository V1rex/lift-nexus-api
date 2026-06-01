package com.v1rex.liftnexus.loadunit.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a load unit")
public enum LoadUnitStatus {
  @Schema(description = "Load unit is expected but not yet physically received")
  EXPECTED,
  @Schema(description = "Load unit is staged at a staging area awaiting put-away")
  STAGED,
  @Schema(description = "Load unit is stored in a storage bin")
  STORED,
  @Schema(description = "Load unit is being moved by a forklift")
  IN_TRANSIT,
  @Schema(description = "Load unit has been shipped out of the warehouse")
  SHIPPED
}
