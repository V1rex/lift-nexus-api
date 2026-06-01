package com.v1rex.liftnexus.transportorder.dto;

import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for updating the status of a transport order")
public record TransportOrderStatusUpdateRequest(
    @NotNull @Schema(description = "New status for the transport order", example = "IN_PROGRESS")
        TransportOrderStatus status) {}
