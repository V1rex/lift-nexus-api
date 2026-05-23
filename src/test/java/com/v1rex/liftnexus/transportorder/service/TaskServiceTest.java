/*
package com.v1rex.liftnexus.transportorder.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.mapper.TransportOrderMapper;
import com.v1rex.liftnexus.transportorder.repository.TransportOrderRepository;
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

  @Mock private TransportOrderRepository taskRepository;
  @Mock private TransportOrderMapper taskMapper;
  @Mock private StorageBinService storageBinService;
  @InjectMocks private TransportOrderService taskService;

  @Nested
  @DisplayName("Create TransportOrder Feature")
  class CreateTask {

    private final Long pickLocationId = 10L;
    private final Long deliveryLocationId = 20L;
    private TransportOrderRequest validRequest;
    private StorageBin pickStorageBin;
    private StorageBin deliveryStorageBin;

    @BeforeEach
    void setUp() {
      validRequest =
          new TransportOrderRequest(pickLocationId, deliveryLocationId, null, EquipmentType.STANDARD, 750);

      pickStorageBin = new StorageBin();
      pickStorageBin.setId(pickLocationId);

      deliveryStorageBin = new StorageBin();
      deliveryStorageBin.setId(deliveryLocationId);
    }

    @Test
    @DisplayName("Should successfully create an OPEN transportorder when locations are valid")
    void createTask_ShouldReturnResponse_WhenRequestIsValid() {
      when(storageBinService.findEntityById(pickLocationId)).thenReturn(pickStorageBin);
      when(storageBinService.findEntityById(deliveryLocationId)).thenReturn(deliveryStorageBin);

      TransportOrder transientTask = new TransportOrder();
      transientTask.setWeight(750);
      when(taskMapper.toEntity(validRequest)).thenReturn(transientTask);

      TransportOrder savedTask = new TransportOrder();
      savedTask.setId(100L);
      savedTask.setStatus(TransportOrderStatus.OPEN);
      savedTask.setWeight(750);
      savedTask.setPickStorageBin(pickStorageBin);
      savedTask.setDeliveryStorageBin(deliveryStorageBin);
      savedTask.setRequiredEquipment(EquipmentType.STANDARD);

      when(taskRepository.save(any(TransportOrder.class))).thenReturn(savedTask);

      StorageBinResponse dummyPick = new StorageBinResponse(pickLocationId, 10.0F, 20.0F);

      StorageBinResponse dummyDelivery = new StorageBinResponse(deliveryLocationId, 20.0F, 30.0F);

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(
              100L, dummyPick, dummyDelivery, 750, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      when(taskMapper.toResponse(savedTask)).thenReturn(mockResponse);

      TransportOrderResponse result = taskService.createTask(validRequest);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(100L);
      assertThat(result.status()).isEqualTo(TransportOrderStatus.OPEN);
      assertThat(result.weight()).isEqualTo(750);
      assertThat(result.requiredEquipment()).isEqualTo(EquipmentType.STANDARD);

      assertThat(result.pickLocation()).isEqualTo(dummyPick);
      assertThat(result.deliveryLocation()).isEqualTo(dummyDelivery);

      verify(taskRepository, times(1)).save(any(TransportOrder.class));
    }
  }

  @Nested
  @DisplayName("Update TransportOrder Feature")
  class UpdateTask {
    private final Long taskId = 42L;
    private TransportOrder existingTask;
    private TransportOrderStatusUpdateRequest openRequest;

    @BeforeEach
    void setUp() {
      existingTask = new TransportOrder();
      existingTask.setId(taskId);
      openRequest = new TransportOrderStatusUpdateRequest(TransportOrderStatus.OPEN);
    }

    @Test
    @DisplayName("Should successfully update transportorder status when transition is valid")
    void updateTask_ShouldReturnResponse_WhenTransitionIsValid() {
      existingTask.setStatus(TransportOrderStatus.OPEN);

      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      TransportOrderStatusUpdateRequest assignedRequest = new TransportOrderStatusUpdateRequest(TransportOrderStatus.ASSIGNED);

      StorageBinResponse dummyPick = new StorageBinResponse(1L, 10.0F, 20.0F);
      StorageBinResponse dummyDeliv = new StorageBinResponse(2L, 12.0F, 22.0F);

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(
              taskId,
              dummyPick,
              dummyDeliv,
              500,
              EquipmentType.STANDARD,
              TransportOrderStatus.ASSIGNED,
              taskId);

      when(taskMapper.toResponse(existingTask)).thenReturn(mockResponse);

      TransportOrderResponse result = taskService.updateTask(taskId, assignedRequest);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(taskId);
      assertThat(result.status()).isEqualTo(TransportOrderStatus.ASSIGNED);
      assertThat(existingTask.getStatus()).isEqualTo(TransportOrderStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when un-assigning an IN_PROGRESS transportorder to OPEN")
    void updateTask_ShouldThrowException_WhenMovingFromInProgressToOpen() {
      existingTask.setStatus(TransportOrderStatus.IN_PROGRESS);
      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      assertThatThrownBy(() -> taskService.updateTask(taskId, openRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot un-assign a transportorder that is already in progress.");
    }

    @Test
    @DisplayName(
        "Should throw IllegalStateException when un-assigning a COMPLETED transportorder to anything else")
    void updateTask_ShouldThrowException_WhenMovingFromCompletedToAnything() {
      existingTask.setStatus(TransportOrderStatus.COMPLETED);
      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      assertThatThrownBy(() -> taskService.updateTask(taskId, openRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot update a transportorder that is already completed.");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when un-assigning an ASSIGNED transportorder to OPEN")
    void updateTask_ShouldThrowException_WhenMovingFromAssignedToOpen() {

      existingTask.setStatus(TransportOrderStatus.ASSIGNED);
      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      assertThatThrownBy(() -> taskService.updateTask(taskId, openRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot un-assign a transportorder that is already assigned.");
    }
  }

  @Nested
  @DisplayName("Find TransportOrder By Id Feature")
  class FindById {
    private final Long taskId = 99L;

    @Test
    @DisplayName("Should return TransportOrderResponse when transportorder exists")
    void findById_ShouldReturnResponse_WhenTaskExists() {
      TransportOrder existingTask = new TransportOrder();
      existingTask.setId(taskId);
      existingTask.setStatus(TransportOrderStatus.OPEN);

      when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(taskId, null, null, 0, null, TransportOrderStatus.OPEN, null);

      when(taskMapper.toResponse(existingTask)).thenReturn(mockResponse);

      TransportOrderResponse result = taskService.findById(taskId);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(taskId);
      assertThat(result.status()).isEqualTo(TransportOrderStatus.OPEN);

      verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transportorder does not exist")
    void findById_ShouldThrowException_WhenTaskDoesNotExist() {
      when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> taskService.findById(taskId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("TransportOrder with " + taskId + " not found.");

      verifyNoInteractions(taskMapper);
    }
  }

  @Nested
  @DisplayName("Search Tasks Feature")
  class SearchTasks {

    @Test
    @DisplayName("Should return paginated TaskResponses matching criteria")
    void searchTasks_ShouldReturnPage_WhenCriteriaIsValid() {
      TransportOrderStatus status = TransportOrderStatus.OPEN;
      Integer minWeight = 500;
      Pageable pageable = Pageable.unpaged();

      TransportOrder transportorder = new TransportOrder();
      transportorder.setId(100L);

      Page<TransportOrder> mockTaskPage =
          new org.springframework.data.domain.PageImpl<>(java.util.List.of(transportorder));

      when(taskRepository.searchTasks(status, minWeight, pageable)).thenReturn(mockTaskPage);

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(100L, null, null, 600, null, TransportOrderStatus.OPEN, null);

      when(taskMapper.toResponse(transportorder)).thenReturn(mockResponse);

      Page<TransportOrderResponse> result = taskService.searchTasks(status, minWeight, pageable);

      assertThat(result).isNotNull();

      assertThat(result.getTotalElements()).isEqualTo(1);

      TransportOrderResponse mappedResponse = result.getContent().get(0);
      assertThat(mappedResponse.id()).isEqualTo(100L);
      assertThat(mappedResponse.status()).isEqualTo(TransportOrderStatus.OPEN);
      assertThat(mappedResponse.weight()).isEqualTo(600);

      verify(taskRepository, times(1)).searchTasks(status, minWeight, pageable);
      verify(taskMapper, times(1)).toResponse(transportorder);
    }
  }
}
*/
