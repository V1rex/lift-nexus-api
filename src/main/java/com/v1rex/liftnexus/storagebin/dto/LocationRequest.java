package com.v1rex.liftnexus.storagebin.dto;

import jakarta.validation.constraints.NotNull;

public record LocationRequest(@NotNull Float latitude, @NotNull Float longitude) {}
