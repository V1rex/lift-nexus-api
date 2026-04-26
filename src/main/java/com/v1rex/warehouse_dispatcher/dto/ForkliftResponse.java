package com.v1rex.warehouse_dispatcher.dto;

import java.util.List;

public record ForkliftResponse(
        Long id,
        Integer weightCapacity,
        List<TaskResponse> tasks,
        LocationResponse currentLocation
) {}
