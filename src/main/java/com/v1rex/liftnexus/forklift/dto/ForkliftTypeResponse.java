package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed view of a forklift type/model in the catalog")
public record ForkliftTypeResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Manufacturer model name", example = "Toyota BT Staxio") String modelName,
    @Schema(description = "Category of material handling equipment") EquipmentType equipmentType,
    @Schema(description = "Maximum safe load capacity in kg", example = "1500")
        Integer maxCapacityKg,
    @Schema(description = "Total battery capacity in kWh", example = "48.0")
        Double totalBatteryCapacitykWh,
    @Schema(description = "Base energy consumption per meter in kWh", example = "0.05")
        Double baseEnergyConsumptionPerMeter) {}
