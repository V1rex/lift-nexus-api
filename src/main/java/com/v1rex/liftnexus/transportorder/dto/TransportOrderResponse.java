package com.v1rex.liftnexus.transportorder.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed view of a transport order")
public record TransportOrderResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Tracking code of the referenced load unit", example = "LU-2024-001")
        String trackingCode,
    @Schema(description = "ID of the load unit being transported", example = "1")
        Long targetLoadUnitId,
    @Schema(description = "ID of the destination storage bin", example = "12") Long targetBinId,
    @Schema(description = "ID of the source/pickup storage bin", example = "5") Long sourceBinId,
    @Schema(description = "Required equipment type", nullable = true)
        EquipmentType requiredEquipment,
    @Schema(description = "Current status of the transport order") TransportOrderStatus status,
    @Schema(
            description = "ID of the forklift assigned to this order",
            example = "2",
            nullable = true)
        Long assignedForkliftId) {}
