package com.v1rex.warehouse_dispatcher.task.service;


import com.v1rex.warehouse_dispatcher.location.domain.Location;
import com.v1rex.warehouse_dispatcher.task.dto.TaskRequest;
import com.v1rex.warehouse_dispatcher.task.dto.TaskStatusUpdateRequest;
import com.v1rex.warehouse_dispatcher.task.enums.TaskStatus;
import com.v1rex.warehouse_dispatcher.common.exception.ResourceNotFoundException;
import com.v1rex.warehouse_dispatcher.location.service.LocationService;
import com.v1rex.warehouse_dispatcher.task.mapper.TaskMapper;
import com.v1rex.warehouse_dispatcher.task.repository.TaskRepository;
import com.v1rex.warehouse_dispatcher.task.dto.TaskResponse;
import com.v1rex.warehouse_dispatcher.task.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    private final LocationService locationService;

    @Transactional
    public TaskResponse createTask(TaskRequest request){
        log.info("Creating task with pickLocationId: {} and deliveryLocationId: {}",
                request.pickLocationId(), request.deliveryLocationId());

        Location pickLocation = locationService.findEntityById(request.pickLocationId());
        Location deliveryLocation = locationService.findEntityById(request.deliveryLocationId());

        Task task = taskMapper.toEntity(request);

        // we set always new tasks to OPEN
        task.setStatus(TaskStatus.OPEN);
        task.setPickLocation(pickLocation);
        task.setDeliveryLocation(deliveryLocation);


        Task savedTask = taskRepository.save(task);

        log.info("Successfully created Task with Id: {}, pickLocationId: {} and deliveryLocationId: {}",
        savedTask.getId(), savedTask.getPickLocation().getId(), savedTask.getDeliveryLocation().getId());

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskStatusUpdateRequest newStatusRequest){
        log.info("Updating task with id: {} to new status: {}", id, newStatusRequest.status());

        Task task = findEntityById(id);

        TaskStatus currentStatus = task.getStatus();
        TaskStatus newStatus = newStatusRequest.status();
        checkStatusBeforeUpdate(id, currentStatus, newStatus );
        // update the status of the task
        task.setStatus(newStatus);


        log.info("Successfully updated Task {}: {} -> {}",
                        task.getId(),
                        currentStatus,
                        newStatus);

        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(findEntityById(id));
    }


     @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(TaskStatus status,
                                          Integer minWeight,
                                          Pageable pageable) {
        return taskRepository.searchTasks(status, minWeight, pageable)
                .map(taskMapper::toResponse);
    }


     public Task findEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() ->{
                    log.warn("Task with id: {} not found.", id);
                    return new ResourceNotFoundException("Task with " + id + " not found.");} );
    }

     private void checkStatusBeforeUpdate(Long id, TaskStatus currentStatus, TaskStatus newStatus){
                if (currentStatus == TaskStatus.COMPLETED) {
                    log.error("Cannot update Task with id: {} because it is already completed.", id);
                    throw new IllegalStateException("Cannot update a" +
                            " task that is already completed.");
                    }

                if (currentStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.OPEN) {
                    log.error("Cannot update Task with id: {} from in progress to open.", id);
                    throw new IllegalStateException("Cannot un-assign a " +
                            "task that is already in progress.");
                }

                if (currentStatus == TaskStatus.ASSIGNED && newStatus == TaskStatus.OPEN) {
                    log.error("Cannot update Task with id: {} from assigned to open.", id);
                    throw new IllegalStateException("Cannot un-assign a " +
                            "task that is already assigned.");
                }
     }

}
