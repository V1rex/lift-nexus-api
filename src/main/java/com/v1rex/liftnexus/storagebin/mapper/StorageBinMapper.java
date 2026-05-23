package com.v1rex.liftnexus.storagebin.mapper;

import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.CoordinateDto;
import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import org.springframework.stereotype.Component;

@Component
public class StorageBinMapper {

  public StorageBin toEntity(StorageBinRequest request) {
    if (request == null) return null;

    Coordinate3D domainCoordinate = null;
    if (request.coordinate() != null) {
      domainCoordinate =
          new Coordinate3D(
              request.coordinate().x(), request.coordinate().y(), request.coordinate().z());
    }

    return StorageBin.builder()
        .binCode(request.binCode())
        .coordinate(domainCoordinate)
        .zoneType(request.zoneType())
        .maxWeightCapacityKg(request.maxWeightCapacityKg())
        .build();
  }

  public StorageBinResponse toResponse(StorageBin entity) {
    if (entity == null) return null;

    CoordinateDto dtoCoordinate = null;
    if (entity.getCoordinate() != null) {
      dtoCoordinate =
          new CoordinateDto(
              entity.getCoordinate().getX(),
              entity.getCoordinate().getY(),
              entity.getCoordinate().getZ());
    }

    return new StorageBinResponse(
        entity.getId(),
        entity.getBinCode(),
        dtoCoordinate,
        entity.getZoneType(),
        entity.getMaxWeightCapacityKg());
  }
}
