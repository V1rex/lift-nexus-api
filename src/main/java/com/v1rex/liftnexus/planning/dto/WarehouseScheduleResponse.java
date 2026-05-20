package com.v1rex.liftnexus.planning.dto;

import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.location.dto.LocationResponse;
import com.v1rex.liftnexus.task.dto.TaskResponse;

import java.util.List;

public record WarehouseScheduleResponse(
        List<LocationResponse> locations,
        List<ForkliftResponse> forkLifts,
        List<TaskResponse> unassignedTasks) {


}
