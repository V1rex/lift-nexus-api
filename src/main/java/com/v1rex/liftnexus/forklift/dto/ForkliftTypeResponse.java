package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;

public record ForkliftTypeResponse(
    Long id,
    String modelName,
    EquipmentType equipmentType,
    Integer maxCapacityKg,
    Double totalBatteryCapacitykWh,
    Double baseEnergyConsumptionPerMeter) {}
