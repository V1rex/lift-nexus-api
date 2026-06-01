package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for registering a new forklift")
public record ForkliftRequest(
    @NotBlank
        @Schema(description = "Unique fleet number identifying the forklift", example = "FL-0042")
        String fleetNumber,
    @NotNull @Schema(description = "ID of the forklift type/model", example = "1")
        Long forkliftTypeId,
    @Schema(
            description = "ID of the storage bin where the forklift is currently located",
            example = "5",
            nullable = true)
        Long currentStorageBinId,
    @Schema(description = "Initial operational status", example = "ACTIVE", nullable = true)
        OperationalStatus status,
    @Schema(
            description = "Current battery level as a percentage (0-100)",
            example = "85.5",
            nullable = true)
        Double currentBatteryPercentage) {}
