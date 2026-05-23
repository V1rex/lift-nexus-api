package com.v1rex.liftnexus.task.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.task.enums.TaskStatus;

public record TaskResponse(
    Long id,
    StorageBinResponse pickLocation,
    StorageBinResponse deliveryLocation,
    Integer weight,
    EquipmentType requiredEquipment,
    TaskStatus status,
    Long forkliftId) {}
