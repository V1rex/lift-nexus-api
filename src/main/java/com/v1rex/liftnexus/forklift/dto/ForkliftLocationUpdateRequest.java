package com.v1rex.liftnexus.forklift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for updating a forklift's location")
public record ForkliftLocationUpdateRequest(
    @NotNull @Schema(description = "ID of the destination storage bin", example = "7")
        Long locationId) {}
