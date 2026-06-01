package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Detailed view of a forklift asset")
public record ForkliftResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Unique fleet number", example = "FL-0042") String fleetNumber,
    @Schema(description = "ID of the assigned forklift type", example = "1") Long forkliftTypeId,
    @Schema(
            description = "Human-readable model name from the forklift type",
            example = "Toyota BT Staxio")
        String modelName,
    @Schema(description = "Type of material handling equipment") EquipmentType equipmentType,
    @Schema(description = "Maximum carrying capacity in kg", example = "1500")
        Integer maxCapacityKg,
    @Schema(description = "ID of the current storage bin location", example = "3", nullable = true)
        Long currentStorageBinId,
    @Schema(description = "Current operational status") OperationalStatus status,
    @Schema(description = "Current battery level percentage", example = "72.3", nullable = true)
        Double currentBatteryPercentage,
    @Schema(description = "IDs of currently assigned transport orders", example = "[10, 11]")
        List<Long> transportOrderIds) {}
