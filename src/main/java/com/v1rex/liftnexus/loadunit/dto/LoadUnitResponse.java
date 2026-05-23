package com.v1rex.liftnexus.loadunit.dto;

import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;

public record LoadUnitResponse(
    Long id,
    String trackingCode,
    int weightKg,
    LoadUnitStatus status,
    Long currentStorageBinId, // Just returning the ID keeps the JSON payload lean
    Long version) {}
