package com.v1rex.liftnexus.transportorder.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import jakarta.validation.constraints.NotNull;

public record TransportOrderRequest(
    @NotNull Long targetLoadUnitId,
    @NotNull Long sourceBinId,
    @NotNull Long destinationBinId,
    EquipmentType requiredEquipment) {}
