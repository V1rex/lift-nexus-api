package com.v1rex.liftnexus.transportorder.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.service.LoadUnitService;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.mapper.TransportOrderMapper;
import com.v1rex.liftnexus.transportorder.repository.TransportOrderRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransportOrderService Business Logic Tests")
class TransportOrderServiceTest {

  @Mock private TransportOrderRepository transportOrderRepository;
  @Mock private TransportOrderMapper transportOrderMapper;
  @Mock private StorageBinService storageBinService;
  @Mock private LoadUnitService loadUnitService;

  @InjectMocks private TransportOrderService transportOrderService;

  @Nested
  @DisplayName("Tests - createTransportOrder Method")
  class CreateOrder {

    @Test
    @DisplayName("Should successfully validate physical locations and create order")
    void shouldCreateOrderSuccessfully() {
      TransportOrderRequest request =
          new TransportOrderRequest(10L, 1L, 2L, EquipmentType.STANDARD);

      StorageBin sourceBin = StorageBin.builder().id(1L).build();

      StorageBin targetBin = StorageBin.builder().id(2L).build();

      LoadUnit loadUnit = LoadUnit.builder().id(10L).currentBin(sourceBin).build();

      TransportOrder mappedEntity = new TransportOrder();

      TransportOrder savedEntity = TransportOrder.builder().id(99L).build();

      TransportOrderResponse expectedResponse =
          new TransportOrderResponse(
              99L, "LU-SKU", 10L, 2L, 1L, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      when(loadUnitService.findEntityById(10L)).thenReturn(loadUnit);

      when(storageBinService.findEntityById(1L)).thenReturn(sourceBin);

      when(storageBinService.findEntityById(2L)).thenReturn(targetBin);

      when(transportOrderMapper.toEntity(request)).thenReturn(mappedEntity);
      when(transportOrderRepository.save(any())).thenReturn(savedEntity);
      when(transportOrderMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

      TransportOrderResponse response = transportOrderService.createTransportOrder(request);

      assertThat(response.id()).isEqualTo(99L);
      verify(transportOrderRepository).save(any(TransportOrder.class));
    }

    @Test
    @DisplayName("Should throw exception if LoadUnit currently has no bin assigned (null)")
    void shouldThrowException_WhenLoadUnitHasNoBin() {
      TransportOrderRequest request =
          new TransportOrderRequest(10L, 1L, 2L, EquipmentType.STANDARD);

      StorageBin sourceBin = StorageBin.builder().id(1L).build();
      StorageBin targetBin = StorageBin.builder().id(2L).build();
      LoadUnit loadUnit =
          LoadUnit.builder().id(10L).trackingCode("LU-NULL").currentBin(null).build();

      when(loadUnitService.findEntityById(10L)).thenReturn(loadUnit);
      when(storageBinService.findEntityById(1L)).thenReturn(sourceBin);
      when(storageBinService.findEntityById(2L)).thenReturn(targetBin);

      assertThatThrownBy(() -> transportOrderService.createTransportOrder(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("LoadUnit LU-NULL is not located in the requested source bin.");

      verify(transportOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception if LoadUnit is in a different physical bin than requested")
    void shouldThrowException_WhenLoadUnitInWrongBin() {
      TransportOrderRequest request =
          new TransportOrderRequest(10L, 1L, 2L, EquipmentType.STANDARD);

      StorageBin sourceBin = StorageBin.builder().id(1L).build();

      StorageBin targetBin = StorageBin.builder().id(2L).build();

      StorageBin actualBin = StorageBin.builder().id(99L).build();

      LoadUnit loadUnit =
          LoadUnit.builder().id(10L).trackingCode("LU-WRONG").currentBin(actualBin).build();

      when(loadUnitService.findEntityById(10L)).thenReturn(loadUnit);

      when(storageBinService.findEntityById(1L)).thenReturn(sourceBin);

      when(storageBinService.findEntityById(2L)).thenReturn(targetBin);

      assertThatThrownBy(() -> transportOrderService.createTransportOrder(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("LoadUnit LU-WRONG is not located in the requested source bin.");

      verify(transportOrderRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Tests - updateOrderStatus Method")
  class UpdateOrderStatus {

    @Test
    @DisplayName("Should successfully update status when transition is legally valid")
    void shouldUpdateStatusSuccessfully() {
      TransportOrder order = new TransportOrder();
      order.setId(1L);
      order.setStatus(TransportOrderStatus.OPEN);

      TransportOrderStatusUpdateRequest request =
          new TransportOrderStatusUpdateRequest(TransportOrderStatus.ASSIGNED);

      TransportOrderResponse expectedResponse =
          new TransportOrderResponse(
              1L,
              "LU-123",
              10L,
              2L,
              1L,
              EquipmentType.STANDARD,
              TransportOrderStatus.ASSIGNED,
              null);

      when(transportOrderRepository.findById(1L)).thenReturn(Optional.of(order));

      when(transportOrderMapper.toResponse(order)).thenReturn(expectedResponse);

      TransportOrderResponse response = transportOrderService.updateOrderStatus(1L, request);

      assertThat(response.status()).isEqualTo(TransportOrderStatus.ASSIGNED);
      assertThat(order.getStatus()).isEqualTo(TransportOrderStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Should throw exception if trying to update an already COMPLETED order")
    void shouldThrowException_WhenOrderIsAlreadyCompleted() {
      TransportOrder order = new TransportOrder();
      order.setId(1L);
      order.setStatus(TransportOrderStatus.COMPLETED);

      when(transportOrderRepository.findById(1L)).thenReturn(Optional.of(order));

      TransportOrderStatusUpdateRequest request =
          new TransportOrderStatusUpdateRequest(TransportOrderStatus.OPEN);

      assertThatThrownBy(() -> transportOrderService.updateOrderStatus(1L, request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot update TransportOrder 1 because it is already COMPLETED.");
    }

    @Test
    @DisplayName("Should throw exception if trying to roll back from IN_PROGRESS to OPEN")
    void shouldThrowException_WhenRollingBackFromInProgressToOpen() {
      TransportOrder order = new TransportOrder();
      order.setId(2L);
      order.setStatus(TransportOrderStatus.IN_PROGRESS);

      when(transportOrderRepository.findById(2L)).thenReturn(Optional.of(order));

      TransportOrderStatusUpdateRequest request =
          new TransportOrderStatusUpdateRequest(TransportOrderStatus.OPEN);

      assertThatThrownBy(() -> transportOrderService.updateOrderStatus(2L, request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot roll back TransportOrder 2 from ACTIVE to OPEN.");
    }

    @Test
    @DisplayName("Should throw exception if trying to roll back from ASSIGNED to OPEN")
    void shouldThrowException_WhenRollingBackFromAssignedToOpen() {
      TransportOrder order = new TransportOrder();
      order.setId(3L);
      order.setStatus(TransportOrderStatus.ASSIGNED);

      when(transportOrderRepository.findById(3L)).thenReturn(Optional.of(order));

      TransportOrderStatusUpdateRequest request =
          new TransportOrderStatusUpdateRequest(TransportOrderStatus.OPEN);

      assertThatThrownBy(() -> transportOrderService.updateOrderStatus(3L, request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot roll back TransportOrder 3 from ASSIGNED to OPEN.");
    }
  }

  @Nested
  @DisplayName("Tests - findById & findEntityById Methods")
  class FindById {

    @Test
    @DisplayName("Should map and return Response when order exists")
    void shouldReturnMappedResponse_WhenFound() {
      TransportOrder order = new TransportOrder();
      order.setId(5L);
      TransportOrderResponse expectedResponse =
          new TransportOrderResponse(
              5L, "LU-123", 10L, 2L, 1L, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      when(transportOrderRepository.findById(5L)).thenReturn(Optional.of(order));

      when(transportOrderMapper.toResponse(order)).thenReturn(expectedResponse);

      TransportOrderResponse result = transportOrderService.findById(5L);

      assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ID does not exist in DB")
    void shouldThrowResourceNotFound_WhenMissing() {
      when(transportOrderRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> transportOrderService.findById(99L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("TransportOrder with ID 99 not found.");
    }
  }

  @Nested
  @DisplayName("Tests - searchOrders Method")
  class SearchOrders {

    @Test
    @DisplayName("Should return paginated TransportOrderResponses matching criteria")
    void searchTasks_ShouldReturnPage_WhenCriteriaIsValid() {
      TransportOrderStatus status = TransportOrderStatus.OPEN;
      Integer minWeight = 500;
      Pageable pageable = Pageable.unpaged();

      TransportOrder transportOrder = new TransportOrder();
      transportOrder.setId(100L);

      Page<TransportOrder> mockTaskPage = new PageImpl<>(java.util.List.of(transportOrder));

      when(transportOrderRepository.searchOrders(status, minWeight, pageable))
          .thenReturn(mockTaskPage);

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(
              100L, "LU-123", 10L, 2L, 1L, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      when(transportOrderMapper.toResponse(transportOrder)).thenReturn(mockResponse);

      Page<TransportOrderResponse> result =
          transportOrderService.searchOrders(status, minWeight, pageable);

      assertThat(result).isNotNull();
      assertThat(result.getTotalElements()).isEqualTo(1);

      TransportOrderResponse mappedResponse = result.getContent().get(0);

      assertThat(mappedResponse.id()).isEqualTo(100L);

      assertThat(mappedResponse.status()).isEqualTo(TransportOrderStatus.OPEN);

      verify(transportOrderRepository, times(1)).searchOrders(status, minWeight, pageable);
    }
  }
}
