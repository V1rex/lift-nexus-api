package com.v1rex.liftnexus.forklift.mapper;

import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class ForkliftTypeMapper {

  public ForkliftType toEntity(ForkliftTypeRequest request) {
    if (request == null) return null;
    return ForkliftType.builder()
        .modelName(request.modelName())
        .equipmentType(request.equipmentType())
        .maxCapacityKg(request.maxCapacityKg())
        .totalBatteryCapacitykWh(request.totalBatteryCapacitykWh())
        .baseEnergyConsumptionPerMeter(request.baseEnergyConsumptionPerMeter())
        .build();
  }

  public ForkliftTypeResponse toResponse(ForkliftType entity) {
    if (entity == null) return null;
    return new ForkliftTypeResponse(
        entity.getId(),
        entity.getModelName(),
        entity.getEquipmentType(),
        entity.getMaxCapacityKg(),
        entity.getTotalBatteryCapacitykWh(),
        entity.getBaseEnergyConsumptionPerMeter());
  }
}
