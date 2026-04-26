package com.v1rex.warehouse_dispatcher.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;


public record ForkliftRequest(
        @NotNull @Min(1) Integer weightCapacity
) {}
