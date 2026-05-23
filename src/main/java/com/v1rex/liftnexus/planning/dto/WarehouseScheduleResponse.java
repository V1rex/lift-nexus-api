package com.v1rex.liftnexus.planning.dto;

import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.task.dto.TaskResponse;
import java.util.List;

public record WarehouseScheduleResponse(
    List<StorageBinResponse> locations,
    List<ForkliftResponse> forkLifts,
    List<TaskResponse> unassignedTasks) {}
