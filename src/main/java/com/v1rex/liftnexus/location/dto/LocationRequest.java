package com.v1rex.liftnexus.location.dto;

import jakarta.validation.constraints.NotNull;

public record LocationRequest(
    @NotNull Float latitude,
    @NotNull Float longitude
) {
}
