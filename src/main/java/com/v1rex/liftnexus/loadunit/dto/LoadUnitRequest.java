package com.v1rex.liftnexus.loadunit.dto;

import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoadUnitRequest(
    @NotBlank(message = "Tracking code is required") String trackingCode,
    @Min(value = 0, message = "Weight cannot be negative") int weightKg,
    @NotNull(message = "Initial status is required") LoadUnitStatus status,

    // Can be null. If provided, the Service layer must verify the bin exists.
    Long currentStorageBinId) {}
