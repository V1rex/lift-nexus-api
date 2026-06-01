package com.v1rex.liftnexus.storagebin.dto;

import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for creating a new storage bin")
public record StorageBinRequest(
    @NotNull(message = "Bin code is required")
        @Schema(
            description = "Human-readable bin identifier (e.g., A-01-02-03)",
            example = "A-01-02-03")
        String binCode,
    @NotNull(message = "Coordinates are required")
        @Valid
        @Schema(description = "3D warehouse coordinates (aisle, bay, tier)")
        CoordinateDto coordinate,
    @NotNull(message = "Zone type is required")
        @Schema(description = "Functional zone of the storage bin")
        ZoneType zoneType,
    @NotNull(message = "Max weight capacity is required")
        @Min(value = 0, message = "Weight capacity cannot be negative")
        @Schema(description = "Maximum weight capacity in kg", example = "2000")
        Integer maxWeightCapacityKg) {}
