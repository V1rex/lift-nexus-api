package com.v1rex.warehouse_dispatcher.dto;

import jakarta.validation.constraints.NotNull;

public record ForkLiftLocationUpdateRequest(
        @NotNull Long locationId) {
}
