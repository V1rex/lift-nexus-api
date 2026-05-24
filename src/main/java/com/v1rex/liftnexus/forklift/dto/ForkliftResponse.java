package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import java.util.List;

public record ForkliftResponse(
    Long id,
    String fleetNumber,
    Long forkliftTypeId,
    String modelName,
    EquipmentType equipmentType,
    Integer maxCapacityKg,
    Long currentStorageBinId,
    OperationalStatus status,
    Double currentBatteryPercentage,
    List<Long> transportOrderIds) {}
