package com.v1rex.liftnexus.task.mapper;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.location.mapper.LocationMapper;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.dto.TaskRequest;
import com.v1rex.liftnexus.task.dto.TaskResponse;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor // Automatically injects the LocationMapper
public class TaskMapper {

  private final LocationMapper locationMapper;

  public Task toEntity(TaskRequest request) {
    if (request == null) return null;
    return Task.builder()
        // we note that extracting the pickLocation and deliveryLocation
        // can be taken care of by the LocationService
        // and will be only injected later to seperate the concerns
        .weight(request.weight())
        .status(request.status() != null ? request.status() : TaskStatus.OPEN)
        .requiredEquipment(
            request.requiredEquipment() != null
                ? request.requiredEquipment()
                : EquipmentType.STANDARD)
        .build();
  }

  public TaskResponse toResponse(Task entity) {
    if (entity == null) return null;
    return new TaskResponse(
        entity.getId(),
        locationMapper.toResponse(entity.getPickLocation()),
        locationMapper.toResponse(entity.getDeliveryLocation()),
        entity.getWeight(),
        entity.getRequiredEquipment(),
        entity.getStatus(),
        entity.getForklift() != null ? entity.getForklift().getId() : null);
  }
}
