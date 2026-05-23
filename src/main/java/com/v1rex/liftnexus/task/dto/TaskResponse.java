package com.v1rex.liftnexus.task.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.storagebin.dto.LocationResponse;
import com.v1rex.liftnexus.task.enums.TaskStatus;

public record TaskResponse(
    Long id,
    LocationResponse pickLocation,
    LocationResponse deliveryLocation,
    Integer weight,
    EquipmentType requiredEquipment,
    TaskStatus status,
    Long forkliftId) {}
