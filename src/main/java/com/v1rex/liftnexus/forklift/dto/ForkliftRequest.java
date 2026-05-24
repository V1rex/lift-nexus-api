package com.v1rex.liftnexus.forklift.dto;

import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ForkliftRequest(
    @NotBlank String fleetNumber,
    @NotNull Long forkliftTypeId,
    Long currentStorageBinId,
    OperationalStatus status,
    Double currentBatteryPercentage) {}
