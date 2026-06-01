package com.v1rex.liftnexus.loadunit.dto;

import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for registering a new load unit")
public record LoadUnitRequest(
    @NotBlank(message = "Tracking code is required")
        @Schema(description = "Unique tracking code for the load unit", example = "LU-2024-001")
        String trackingCode,
    @Min(value = 0, message = "Weight cannot be negative")
        @Schema(description = "Weight of the load unit in kg", example = "450")
        int weightKg,
    @NotNull(message = "Initial status is required")
        @Schema(description = "Initial status of the load unit")
        LoadUnitStatus status,
    @Schema(
            description = "ID of the storage bin where the unit is placed (nullable)",
            example = "3",
            nullable = true)
        Long currentStorageBinId) {}
