package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request payload for creating a new forklift type/model in the catalog")
public record ForkliftTypeRequest(
    @NotBlank(message = "Model name is mandatory")
        @Schema(description = "Manufacturer model name", example = "Toyota BT Staxio")
        String modelName,
    @NotNull(message = "Equipment type is mandatory")
        @Schema(description = "Category of material handling equipment")
        EquipmentType equipmentType,
    @NotNull(message = "Max capacity is mandatory")
        @Positive(message = "Max capacity must be greater than zero")
        @Schema(description = "Maximum safe load capacity in kg", example = "1500")
        Integer maxCapacityKg,
    @NotNull(message = "Total battery capacity is mandatory")
        @Positive(message = "Battery capacity must be greater than zero")
        @Schema(description = "Total battery capacity in kWh", example = "48.0")
        Double totalBatteryCapacitykWh,
    @NotNull(message = "Base energy consumption is mandatory")
        @Positive(message = "Energy consumption must be greater than zero")
        @Schema(
            description = "Base energy consumption per meter travelled in kWh",
            example = "0.05")
        Double baseEnergyConsumptionPerMeter) {}
