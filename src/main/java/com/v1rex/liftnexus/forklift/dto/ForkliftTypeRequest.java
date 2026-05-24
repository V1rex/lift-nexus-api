package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ForkliftTypeRequest(
    @NotBlank(message = "Model name is mandatory") String modelName,
    @NotNull(message = "Equipment type is mandatory") EquipmentType equipmentType,
    @NotNull(message = "Max capacity is mandatory")
        @Positive(message = "Max capacity must be greater than zero")
        Integer maxCapacityKg,
    @NotNull(message = "Total battery capacity is mandatory")
        @Positive(message = "Battery capacity must be greater than zero")
        Double totalBatteryCapacitykWh,
    @NotNull(message = "Base energy consumption is mandatory")
        @Positive(message = "Energy consumption must be greater than zero")
        Double baseEnergyConsumptionPerMeter) {}
