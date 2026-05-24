package com.v1rex.liftnexus.forklift.mapper;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ForkliftMapper {

  public Forklift toEntity(ForkliftRequest request) {
    if (request == null) return null;
    return Forklift.builder()
        .fleetNumber(request.fleetNumber())
        .status(request.status() != null ? request.status() : OperationalStatus.OFFLINE)
        .currentBatteryPercentage(
            request.currentBatteryPercentage() != null ? request.currentBatteryPercentage() : 100.0)
        .build();
  }

  public ForkliftResponse toResponse(Forklift entity) {
    if (entity == null) return null;
    return new ForkliftResponse(
        entity.getId(),
        entity.getFleetNumber(),
        entity.getForkliftType() != null ? entity.getForkliftType().getId() : null,
        entity.getForkliftType() != null ? entity.getForkliftType().getModelName() : null,
        entity.getForkliftType() != null ? entity.getForkliftType().getEquipmentType() : null,
        entity.getForkliftType() != null ? entity.getForkliftType().getMaxCapacityKg() : null,
        entity.getCurrentStorageBin() != null ? entity.getCurrentStorageBin().getId() : null,
        entity.getStatus(),
        entity.getCurrentBatteryPercentage(),
        entity.getTransportOrders() != null
            ? entity.getTransportOrders().stream().map(TransportOrder::getId).toList()
            : List.of() // TODO: fix later as this can cause the N+1 Query Problem
        );
  }
}
