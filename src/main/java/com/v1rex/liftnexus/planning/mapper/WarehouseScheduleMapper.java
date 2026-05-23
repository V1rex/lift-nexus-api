package com.v1rex.liftnexus.planning.mapper;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.mapper.ForkliftMapper;
import com.v1rex.liftnexus.planning.dto.WarehouseScheduleResponse;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.mapper.TaskMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseScheduleMapper {

  private final StorageBinMapper storageBinMapper;
  private final ForkliftMapper forkliftMapper;
  private final TaskMapper taskMapper;

  public WarehouseScheduleResponse toResponse(
      List<StorageBin> storageBins, List<Forklift> forklifts, List<Task> unassignedTasks) {

    return new WarehouseScheduleResponse(
        storageBins.stream().map(storageBinMapper::toResponse).toList(),
        forklifts.stream().map(forkliftMapper::toResponse).toList(),
        unassignedTasks.stream().map(taskMapper::toResponse).toList());
  }
}
