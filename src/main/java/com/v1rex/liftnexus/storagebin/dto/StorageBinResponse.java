package com.v1rex.liftnexus.storagebin.dto;

import com.v1rex.liftnexus.storagebin.domain.ZoneType;

public record StorageBinResponse(
    Long id,
    String binCode,
    CoordinateDto coordinate,
    ZoneType zoneType,
    Integer maxWeightCapacityKg) {}
