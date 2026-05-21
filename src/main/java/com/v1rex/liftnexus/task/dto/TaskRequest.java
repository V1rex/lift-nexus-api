package com.v1rex.liftnexus.task.dto;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TaskRequest(
    @NotNull Long pickLocationId,
    @NotNull Long deliveryLocationId,
    TaskStatus status,
    EquipmentType requiredEquipment,
    @NotNull @Min(1) Integer weight) {}
