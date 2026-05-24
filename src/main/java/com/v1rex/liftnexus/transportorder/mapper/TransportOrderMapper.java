package com.v1rex.liftnexus.transportorder.mapper;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import org.springframework.stereotype.Component;

@Component
public class TransportOrderMapper {

  public TransportOrder toEntity(TransportOrderRequest request) {
    if (request == null) return null;
    return TransportOrder.builder()
        .status(TransportOrderStatus.OPEN)
        .requiredEquipment(
            request.requiredEquipment() != null
                ? request.requiredEquipment()
                : EquipmentType.STANDARD)
        .build();
  }

  public TransportOrderResponse toResponse(TransportOrder entity) {
    if (entity == null) return null;
    return new TransportOrderResponse(
        entity.getId(),
        entity.getTargetLoadUnit() != null ? entity.getTargetLoadUnit().getTrackingCode() : null,
        entity.getTargetLoadUnit() != null ? entity.getTargetLoadUnit().getId() : null,
        entity.getTargetBin() != null ? entity.getTargetBin().getId() : null,
        entity.getSourceBin() != null ? entity.getSourceBin().getId() : null,
        entity.getRequiredEquipment(),
        entity.getStatus(),
        entity.getAssignedForklift() != null ? entity.getAssignedForklift().getId() : null);
  }
}
