package com.v1rex.liftnexus.transportorder.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for creating a new transport order")
public record TransportOrderRequest(
    @NotNull @Schema(description = "ID of the load unit to transport", example = "1")
        Long targetLoadUnitId,
    @NotNull @Schema(description = "ID of the source/pickup storage bin", example = "5")
        Long sourceBinId,
    @NotNull @Schema(description = "ID of the destination/drop-off storage bin", example = "12")
        Long destinationBinId,
    @Schema(description = "Required equipment type for this transport (nullable)", nullable = true)
        EquipmentType requiredEquipment) {}
