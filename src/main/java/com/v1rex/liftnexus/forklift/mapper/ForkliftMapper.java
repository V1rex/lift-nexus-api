package com.v1rex.liftnexus.forklift.mapper;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.transportorder.mapper.TransportOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForkliftMapper {

  private final TransportOrderMapper transportOrderMapper;
  private final StorageBinMapper storageBinMapper;

  public ForkliftResponse toResponse(Forklift entity) {
    if (entity == null) return null;
    return new ForkliftResponse(
        entity.getId(),
        entity.getWeightCapacity(),
        entity.getEquipmentType(),
        entity.getTransportOrders().stream().map(transportOrderMapper::toResponse).toList(),
        storageBinMapper.toResponse(entity.getCurrentStorageBin()));
  }

  public Forklift toEntity(ForkliftRequest request) {
    if (request == null) return null;
    return Forklift.builder()
        .weightCapacity(request.weightCapacity())
        .equipmentType(request.equipmentType())
        .build();
  }
}
