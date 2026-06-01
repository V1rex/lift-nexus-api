package com.v1rex.liftnexus.forklift.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category of material handling equipment")
public enum EquipmentType {
  @Schema(description = "Manual or electric pallet jack for horizontal transport")
  PALLET_JACK,
  @Schema(description = "Standard counterbalance forklift")
  STANDARD,
  @Schema(description = "Reach truck for narrow-aisle high-bay operations")
  REACH_TRUCK,
  @Schema(description = "Side loader for long/oversized loads")
  SIDE_LOADER
}
