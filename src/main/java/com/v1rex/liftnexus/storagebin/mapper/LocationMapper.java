package com.v1rex.liftnexus.storagebin.mapper;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.LocationRequest;
import com.v1rex.liftnexus.storagebin.dto.LocationResponse;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

  public StorageBin toEntity(LocationRequest request) {
    if (request == null) return null;
    return StorageBin.builder().longitude(request.longitude()).latitude(request.latitude()).build();
  }

  public LocationResponse toResponse(StorageBin entity) {
    if (entity == null) return null;
    return new LocationResponse(entity.getId(), entity.getLatitude(), entity.getLongitude());
  }
}
