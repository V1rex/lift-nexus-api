package com.v1rex.liftnexus.transportorder.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TransportOrderMapper Unit Tests")
public class TransportOrderMapperTest {

  private final TransportOrderMapper mapper = new TransportOrderMapper();

  @Nested
  @DisplayName("Tests - toResponse Mapping")
  class ToResponseMapping {

    @Test
    @DisplayName("Should return null when TransportOrder Entity is null")
    void shouldReturnNull_WhenEntityIsNull() {
      assertNull(mapper.toResponse(null));
    }

    @Test
    @DisplayName("Should map complete Entity to flat ID Response record correctly")
    void shouldMapEntityToResponseSuccessfully() {
      // Arrange
      StorageBin mockSourceBin = StorageBin.builder().id(1L).build();
      StorageBin mockTargetBin = StorageBin.builder().id(2L).build();
      LoadUnit mockLoadUnit = LoadUnit.builder().id(100L).trackingCode("LU-FLAT").build();
      Forklift mockForklift = Forklift.builder().id(5L).build();

      TransportOrder mockOrder =
          TransportOrder.builder()
              .id(15L)
              .targetLoadUnit(mockLoadUnit)
              .sourceBin(mockSourceBin)
              .targetBin(mockTargetBin)
              .requiredEquipment(EquipmentType.STANDARD)
              .status(TransportOrderStatus.OPEN)
              .assignedForklift(mockForklift)
              .build();

      // Act
      TransportOrderResponse response = mapper.toResponse(mockOrder);

      // Assert
      assertNotNull(response);
      assertEquals(15L, response.id());
      assertEquals("LU-FLAT", response.trackingCode());
      assertEquals(100L, response.targetLoadUnitId());
      assertEquals(1L, response.sourceBinId());
      assertEquals(2L, response.targetBinId());
      assertEquals(EquipmentType.STANDARD, response.requiredEquipment());
      assertEquals(TransportOrderStatus.OPEN, response.status());
      assertEquals(5L, response.assignedForkliftId());
    }
  }

  @Nested
  @DisplayName("Tests - toEntity Mapping")
  class ToEntityMapping {

    @Test
    @DisplayName("Should return null when Request is null")
    void shouldReturnNull_WhenRequestIsNull() {
      assertNull(mapper.toEntity(null));
    }

    @Test
    @DisplayName("Should map Request to fresh Entity with OPEN status")
    void shouldMapRequestToEntity() {
      TransportOrderRequest request =
          new TransportOrderRequest(100L, 1L, 2L, EquipmentType.SIDE_LOADER);

      TransportOrder entity = mapper.toEntity(request);

      assertNotNull(entity);
      assertEquals(TransportOrderStatus.OPEN, entity.getStatus());
      assertEquals(EquipmentType.SIDE_LOADER, entity.getRequiredEquipment());
    }
  }
}
