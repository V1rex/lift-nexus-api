/*
package com.v1rex.liftnexus.transportorder.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskMapperTest {

  @Mock private StorageBinMapper storageBinMapper;

  @InjectMocks private TransportOrderMapper mapper;

  @Nested
  @DisplayName("Tests for toEntity mapping")
  class ToEntityTests {

    @Test
    @DisplayName("Should correctly map TransportOrderRequest to TransportOrder Entity")
    void shouldMapRequestToEntity() {
      TransportOrderRequest request = new TransportOrderRequest(1L, 2L, TransportOrderStatus.OPEN, EquipmentType.STANDARD, 100);

      TransportOrder entity = mapper.toEntity(request);

      assertNotNull(entity);

      assertNull(entity.getId(), "New entities mapped from a request should not have an ID yet");

      assertNull(
          entity.getPickStorageBin(),
          "New entities mapped from a request should not have a pick storagebin yet, as it will be set later by the service");

      assertNull(
          entity.getDeliveryStorageBin(),
          "New entities mapped from a request should not have a delivery storagebin yet, as it will be set later by the service");

      assertNull(
          entity.getForklift(),
          "New entities mapped from a request should not have an assigned forklift yet, as it will be set later by the service");

      assertEquals(request.weight(), entity.getWeight());
      assertEquals(request.status(), entity.getStatus());
      assertEquals(request.requiredEquipment(), entity.getRequiredEquipment());
    }

    @Test
    @DisplayName("Should return null when TransportOrderRequest is null")
    void shouldReturnNull_WhenRequestIsNull() {
      TransportOrder entity = mapper.toEntity(null);

      assertNull(entity);
    }
  }

  @Nested
  @DisplayName("Tests for toResponse mapping")
  class ToResponseTests {

    @Test
    @DisplayName("Should correctly map TransportOrder Entity to TransportOrderResponse DTO")
    void shouldMapEntityToResponse() {
      // Arranging
      StorageBin mockPickStorageBin =
          StorageBin.builder().id(10L).latitude(10.5F).longitude(13.45F).build();

      StorageBin mockDeliveryStorageBin =
          StorageBin.builder().id(2L).latitude(11.5F).longitude(12.45F).build();

      Forklift mockForlift = Forklift.builder().id(5L).weightCapacity(100).build();

      TransportOrder mockTask =
          TransportOrder.builder()
              .id(15L)
              .pickStorageBin(mockPickStorageBin)
              .deliveryStorageBin(mockDeliveryStorageBin)
              .weight(10)
              .requiredEquipment(EquipmentType.STANDARD)
              .status(TransportOrderStatus.OPEN)
              .forklift(mockForlift)
              .build();

      StorageBinResponse mockPickStorageBinResponse = new StorageBinResponse(1L, 10.5F, 13.45F);

      StorageBinResponse mockDeliveryStorageBinResponse = new StorageBinResponse(2L, 11.5F, 12.45F);

      when(storageBinMapper.toResponse(mockPickStorageBin)).thenReturn(mockPickStorageBinResponse);

      when(storageBinMapper.toResponse(mockDeliveryStorageBin))
          .thenReturn(mockDeliveryStorageBinResponse);

      // Act
      var response = mapper.toResponse(mockTask);

      // Assert
      assertNotNull(response);
      assertEquals(15L, response.id());
      assertEquals(mockPickStorageBinResponse, response.pickLocation());
      assertEquals(mockDeliveryStorageBinResponse, response.deliveryLocation());
      assertEquals(10, response.weight());
      assertEquals(EquipmentType.STANDARD, response.requiredEquipment());
      assertEquals(5L, response.forkliftId());
    }

    @Test
    @DisplayName("Should return null when TransportOrder Entity is null")
    void shouldReturnNull_WhenEntityIsNull() {
      TransportOrderResponse response = mapper.toResponse(null);
      assertNull(response);
    }
  }
}
*/
