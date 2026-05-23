/*
package com.v1rex.liftnexus.task.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.dto.TaskRequest;
import com.v1rex.liftnexus.task.dto.TaskResponse;
import com.v1rex.liftnexus.task.dto.TaskStatusUpdateRequest;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import com.v1rex.liftnexus.task.mapper.TaskMapper;
import com.v1rex.liftnexus.task.repository.TaskRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

  @Mock private TaskRepository taskRepository;
  @Mock private TaskMapper taskMapper;
  @Mock private StorageBinService storageBinService;
  @InjectMocks private TaskService taskService;

  @Nested
  @DisplayName("Create Task Feature")
  class CreateTask {

    private final Long pickLocationId = 10L;
    private final Long deliveryLocationId = 20L;
    private TaskRequest validRequest;
    private StorageBin pickStorageBin;
    private StorageBin deliveryStorageBin;

    @BeforeEach
    void setUp() {
      validRequest =
          new TaskRequest(pickLocationId, deliveryLocationId, null, EquipmentType.STANDARD, 750);

      pickStorageBin = new StorageBin();
      pickStorageBin.setId(pickLocationId);

      deliveryStorageBin = new StorageBin();
      deliveryStorageBin.setId(deliveryLocationId);
    }

    @Test
    @DisplayName("Should successfully create an OPEN task when locations are valid")
    void createTask_ShouldReturnResponse_WhenRequestIsValid() {
      when(storageBinService.findEntityById(pickLocationId)).thenReturn(pickStorageBin);
      when(storageBinService.findEntityById(deliveryLocationId)).thenReturn(deliveryStorageBin);

      Task transientTask = new Task();
      transientTask.setWeight(750);
      when(taskMapper.toEntity(validRequest)).thenReturn(transientTask);

      Task savedTask = new Task();
      savedTask.setId(100L);
      savedTask.setStatus(TaskStatus.OPEN);
      savedTask.setWeight(750);
      savedTask.setPickStorageBin(pickStorageBin);
      savedTask.setDeliveryStorageBin(deliveryStorageBin);
      savedTask.setRequiredEquipment(EquipmentType.STANDARD);

      when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

      StorageBinResponse dummyPick = new StorageBinResponse(pickLocationId, 10.0F, 20.0F);

      StorageBinResponse dummyDelivery = new StorageBinResponse(deliveryLocationId, 20.0F, 30.0F);

      TaskResponse mockResponse =
          new TaskResponse(
              100L, dummyPick, dummyDelivery, 750, EquipmentType.STANDARD, TaskStatus.OPEN, null);

      when(taskMapper.toResponse(savedTask)).thenReturn(mockResponse);

      TaskResponse result = taskService.createTask(validRequest);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(100L);
      assertThat(result.status()).isEqualTo(TaskStatus.OPEN);
      assertThat(result.weight()).isEqualTo(750);
      assertThat(result.requiredEquipment()).isEqualTo(EquipmentType.STANDARD);

      assertThat(result.pickLocation()).isEqualTo(dummyPick);
      assertThat(result.deliveryLocation()).isEqualTo(dummyDelivery);

      verify(taskRepository, times(1)).save(any(Task.class));
    }
  }

  @Nested
  @DisplayName("Update Task Feature")
  class UpdateTask {
    private final Long taskId = 42L;
    private Task existingTask;
    private TaskStatusUpdateRequest openRequest;

    @BeforeEach
    void setUp() {
      existingTask = new Task();
      existingTask.setId(taskId);
      openRequest = new TaskStatusUpdateRequest(TaskStatus.OPEN);
    }

    @Test
    @DisplayName("Should successfully update task status when transition is valid")
    void updateTask_ShouldReturnResponse_WhenTransitionIsValid() {
      existingTask.setStatus(TaskStatus.OPEN);

      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      TaskStatusUpdateRequest assignedRequest = new TaskStatusUpdateRequest(TaskStatus.ASSIGNED);

      StorageBinResponse dummyPick = new StorageBinResponse(1L, 10.0F, 20.0F);
      StorageBinResponse dummyDeliv = new StorageBinResponse(2L, 12.0F, 22.0F);

      TaskResponse mockResponse =
          new TaskResponse(
              taskId,
              dummyPick,
              dummyDeliv,
              500,
              EquipmentType.STANDARD,
              TaskStatus.ASSIGNED,
              taskId);

      when(taskMapper.toResponse(existingTask)).thenReturn(mockResponse);

      TaskResponse result = taskService.updateTask(taskId, assignedRequest);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(taskId);
      assertThat(result.status()).isEqualTo(TaskStatus.ASSIGNED);
      assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when un-assigning an IN_PROGRESS task to OPEN")
    void updateTask_ShouldThrowException_WhenMovingFromInProgressToOpen() {
      existingTask.setStatus(TaskStatus.IN_PROGRESS);
      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      assertThatThrownBy(() -> taskService.updateTask(taskId, openRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot un-assign a task that is already in progress.");
    }

    @Test
    @DisplayName(
        "Should throw IllegalStateException when un-assigning a COMPLETED task to anything else")
    void updateTask_ShouldThrowException_WhenMovingFromCompletedToAnything() {
      existingTask.setStatus(TaskStatus.COMPLETED);
      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      assertThatThrownBy(() -> taskService.updateTask(taskId, openRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot update a task that is already completed.");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when un-assigning an ASSIGNED task to OPEN")
    void updateTask_ShouldThrowException_WhenMovingFromAssignedToOpen() {

      existingTask.setStatus(TaskStatus.ASSIGNED);
      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      assertThatThrownBy(() -> taskService.updateTask(taskId, openRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot un-assign a task that is already assigned.");
    }
  }

  @Nested
  @DisplayName("Find Task By Id Feature")
  class FindById {
    private final Long taskId = 99L;

    @Test
    @DisplayName("Should return TaskResponse when task exists")
    void findById_ShouldReturnResponse_WhenTaskExists() {
      Task existingTask = new Task();
      existingTask.setId(taskId);
      existingTask.setStatus(TaskStatus.OPEN);

      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      TaskResponse mockResponse =
          new TaskResponse(taskId, null, null, 0, null, TaskStatus.OPEN, null);

      when(taskMapper.toResponse(existingTask)).thenReturn(mockResponse);

      TaskResponse result = taskService.findById(taskId);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(taskId);
      assertThat(result.status()).isEqualTo(TaskStatus.OPEN);

      verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task does not exist")
    void findById_ShouldThrowException_WhenTaskDoesNotExist() {
      when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> taskService.findById(taskId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Task with " + taskId + " not found.");

      verifyNoInteractions(taskMapper);
    }
  }

  @Nested
  @DisplayName("Search Tasks Feature")
  class SearchTasks {

    @Test
    @DisplayName("Should return paginated TaskResponses matching criteria")
    void searchTasks_ShouldReturnPage_WhenCriteriaIsValid() {
      TaskStatus status = TaskStatus.OPEN;
      Integer minWeight = 500;
      Pageable pageable = Pageable.unpaged();

      Task task = new Task();
      task.setId(100L);

      Page<Task> mockTaskPage =
          new org.springframework.data.domain.PageImpl<>(java.util.List.of(task));

      when(taskRepository.searchTasks(status, minWeight, pageable)).thenReturn(mockTaskPage);

      TaskResponse mockResponse =
          new TaskResponse(100L, null, null, 600, null, TaskStatus.OPEN, null);

      when(taskMapper.toResponse(task)).thenReturn(mockResponse);

      Page<TaskResponse> result = taskService.searchTasks(status, minWeight, pageable);

      assertThat(result).isNotNull();

      assertThat(result.getTotalElements()).isEqualTo(1);

      TaskResponse mappedResponse = result.getContent().get(0);
      assertThat(mappedResponse.id()).isEqualTo(100L);
      assertThat(mappedResponse.status()).isEqualTo(TaskStatus.OPEN);
      assertThat(mappedResponse.weight()).isEqualTo(600);

      verify(taskRepository, times(1)).searchTasks(status, minWeight, pageable);
      verify(taskMapper, times(1)).toResponse(task);
    }
  }
}
*/
