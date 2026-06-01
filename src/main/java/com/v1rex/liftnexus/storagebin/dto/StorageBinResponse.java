package com.v1rex.liftnexus.storagebin.dto;

import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed view of a storage bin location")
public record StorageBinResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Human-readable bin identifier", example = "A-01-02-03") String binCode,
    @Schema(description = "3D warehouse coordinates") CoordinateDto coordinate,
    @Schema(description = "Functional zone of the storage bin") ZoneType zoneType,
    @Schema(description = "Maximum weight capacity in kg", example = "2000")
        Integer maxWeightCapacityKg) {}
