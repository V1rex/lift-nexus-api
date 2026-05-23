package com.v1rex.liftnexus.loadunit.mapper;

import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import org.springframework.stereotype.Component;

@Component
public class LoadUnitMapper {

  public LoadUnit toEntity(LoadUnitRequest request, StorageBin currentBin) {
    if (request == null) {
      return null;
    }
    return LoadUnit.builder()
        .trackingCode(request.trackingCode())
        .weightKg(request.weightKg())
        .status(request.status())
        .currentBin(currentBin)
        .build();
  }

  public LoadUnitResponse toResponse(LoadUnit entity) {
    if (entity == null) {
      return null;
    }
    Long binId = entity.getCurrentBin() != null ? entity.getCurrentBin().getId() : null;

    return new LoadUnitResponse(
        entity.getId(),
        entity.getTrackingCode(),
        entity.getWeightKg(),
        entity.getStatus(),
        binId,
        entity.getVersion());
  }
}
