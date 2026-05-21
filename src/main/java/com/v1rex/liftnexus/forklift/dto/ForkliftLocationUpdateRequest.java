package com.v1rex.liftnexus.forklift.dto;

import jakarta.validation.constraints.NotNull;

public record ForkliftLocationUpdateRequest(@NotNull Long locationId) {}
