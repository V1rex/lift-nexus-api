package com.v1rex.liftnexus.planning.mapper;

import com.v1rex.liftnexus.forklift.mapper.ForkliftMapper;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.planning.dto.WarehouseScheduleResponse;
import com.v1rex.liftnexus.location.mapper.LocationMapper;
import com.v1rex.liftnexus.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WarehouseScheduleMapper {

    private final LocationMapper locationMapper;
    private final ForkliftMapper forkliftMapper;
    private final TaskMapper taskMapper;

    public WarehouseScheduleResponse toResponse(
            List<Location> locations,
            List<Forklift> forklifts,
            List<Task> unassignedTasks) {

        return new WarehouseScheduleResponse(
                locations.stream().map(locationMapper::toResponse).toList(),
                forklifts.stream().map(forkliftMapper::toResponse).toList(),
                unassignedTasks.stream().map(taskMapper::toResponse).toList()
        );
    }
}