package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import java.util.List;

public record ForkliftResponse(
    Long id,
    Integer weightCapacity,
    EquipmentType equipmentType,
    List<TransportOrderResponse> tasks,
    StorageBinResponse currentLocation) {}
