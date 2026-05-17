package com.v1rex.warehouse_dispatcher.location.dto;

import jakarta.validation.constraints.NotNull;

public record LocationRequest(
    @NotNull Float latitude,
    @NotNull Float longitude
) {
}
