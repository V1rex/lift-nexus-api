package com.v1rex.liftnexus.transportorder.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;

public record TransportOrderResponse(
    Long id,
    String trackingCode, //  can be pulled from LoadUnit
    Long targetLoadUnitId,
    Long targetBinId,
    Long sourceBinId,
    EquipmentType requiredEquipment,
    TransportOrderStatus status,
    Long assignedForkliftId) {}
