package com.v1rex.liftnexus.task.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import com.v1rex.liftnexus.location.dto.LocationResponse;

public record TaskResponse(
        Long id,
        LocationResponse pickLocation,
        LocationResponse deliveryLocation,
        Integer weight,
        EquipmentType requiredEquipment,
        TaskStatus status,
        Long forkliftId
) {

}
