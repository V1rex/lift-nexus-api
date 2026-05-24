package com.v1rex.liftnexus.transportorder.dto;

import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import jakarta.validation.constraints.NotNull;

public record TransportOrderStatusUpdateRequest(@NotNull TransportOrderStatus status) {}
