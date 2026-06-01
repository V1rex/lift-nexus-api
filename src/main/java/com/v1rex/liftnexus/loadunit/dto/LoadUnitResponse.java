package com.v1rex.liftnexus.loadunit.dto;

import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed view of a load unit")
public record LoadUnitResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Unique tracking code", example = "LU-2024-001") String trackingCode,
    @Schema(description = "Weight in kg", example = "450") int weightKg,
    @Schema(description = "Current lifecycle status of the load unit") LoadUnitStatus status,
    @Schema(
            description = "ID of the storage bin where the unit is located",
            example = "3",
            nullable = true)
        Long currentStorageBinId,
    @Schema(description = "Optimistic locking version number", example = "0") Long version) {}
