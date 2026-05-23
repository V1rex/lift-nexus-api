package com.v1rex.liftnexus.storagebin.dto;

import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StorageBinRequest(
    @NotNull(message = "Bin code is required") String binCode,
    @NotNull(message = "Coordinates are required") @Valid CoordinateDto coordinate,
    @NotNull(message = "Zone type is required") ZoneType zoneType,
    @NotNull(message = "Max weight capacity is required")
        @Min(value = 0, message = "Weight capacity cannot be negative")
        Integer maxWeightCapacityKg) {}
