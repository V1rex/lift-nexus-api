package com.v1rex.liftnexus.storagebin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "3D coordinate representing a location in the warehouse (aisle, bay, tier)")
public record CoordinateDto(
    @NotNull(message = "X coordinate (aisle) is required")
        @Schema(description = "Aisle number", example = "1")
        Integer x,
    @NotNull(message = "Y coordinate (bay) is required")
        @Schema(description = "Bay/column number within the aisle", example = "2")
        Integer y,
    @NotNull(message = "Z coordinate (tier) is required")
        @Schema(description = "Tier/level number", example = "3")
        Integer z) {}
