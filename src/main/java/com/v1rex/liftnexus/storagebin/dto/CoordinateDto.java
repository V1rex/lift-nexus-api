package com.v1rex.liftnexus.storagebin.dto;

import jakarta.validation.constraints.NotNull;

public record CoordinateDto(
    @NotNull(message = "X coordinate (aisle) is required") Integer x,
    @NotNull(message = "Y coordinate (bay) is required") Integer y,
    @NotNull(message = "Z coordinate (tier) is required") Integer z) {}
