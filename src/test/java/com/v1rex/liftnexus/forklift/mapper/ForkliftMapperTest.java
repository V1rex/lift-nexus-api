package com.v1rex.liftnexus.forklift.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ForkliftMapperTest {

  private final ForkliftMapper mapper = new ForkliftMapper();

  @Nested
  @DisplayName("Tests - toEntity(ForkliftRequest)")
  class ToEntityTest {

    @Test
    @DisplayName("Should map request to entity with default fallback values")
    void shouldMapToEntityWithDefaults() {
      ForkliftRequest request = new ForkliftRequest("FL-01", 1L, null, null, null);

      Forklift entity = mapper.toEntity(request);

      assertNotNull(entity);
      assertEquals("FL-01", entity.getFleetNumber());
      assertEquals(OperationalStatus.OFFLINE, entity.getStatus()); // Default branch hit
      assertEquals(100.0, entity.getCurrentBatteryPercentage()); // Default branch hit
    }

    @Test
    @DisplayName("Should map request to entity with explicit values")
    void shouldMapToEntityWithExplicitValues() {
      ForkliftRequest request =
          new ForkliftRequest("FL-02", 1L, 5L, OperationalStatus.ACTIVE, 85.5);

      Forklift entity = mapper.toEntity(request);

      assertEquals("FL-02", entity.getFleetNumber());
      assertEquals(OperationalStatus.ACTIVE, entity.getStatus());
      assertEquals(85.5, entity.getCurrentBatteryPercentage());
    }

    @Test
    @DisplayName("Should return null when request is null")
    void shouldReturnNullWhenRequestIsNull() {
      assertNull(mapper.toEntity(null));
    }
  }

  @Nested
  @DisplayName("Tests - toResponse(Forklift)")
  class ToResponseTest {

    @Test
    @DisplayName("Should map fully populated entity to response")
    void shouldMapPopulatedEntity() {
      ForkliftType type =
          ForkliftType.builder()
              .id(10L)
              .modelName("Toyota X")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(2000)
              .build();
      StorageBin bin = StorageBin.builder().id(55L).build();
      TransportOrder order = TransportOrder.builder().id(100L).build();

      Forklift entity =
          Forklift.builder()
              .id(1L)
              .fleetNumber("FL-01")
              .forkliftType(type)
              .currentStorageBin(bin)
              .status(OperationalStatus.ACTIVE)
              .currentBatteryPercentage(90.0)
              .transportOrders(List.of(order))
              .build();

      ForkliftResponse response = mapper.toResponse(entity);

      assertEquals(1L, response.id());
      assertEquals("FL-01", response.fleetNumber());
      assertEquals(10L, response.forkliftTypeId());
      assertEquals("Toyota X", response.modelName());
      assertEquals(EquipmentType.STANDARD, response.equipmentType());
      assertEquals(2000, response.maxCapacityKg());
      assertEquals(55L, response.currentStorageBinId());
      assertEquals(OperationalStatus.ACTIVE, response.status());
      assertEquals(90.0, response.currentBatteryPercentage());
      assertTrue(response.transportOrderIds().contains(100L));
    }

    @Test
    @DisplayName("Should handle entity with null relationships safely")
    void shouldHandleNullRelationships() {
      Forklift entity =
          Forklift.builder().id(2L).fleetNumber("FL-02").build(); // No relationships attached

      ForkliftResponse response = mapper.toResponse(entity);

      assertNull(response.forkliftTypeId());
      assertNull(response.currentStorageBinId());
      assertTrue(response.transportOrderIds().isEmpty()); // Null safe list check
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
      assertNull(mapper.toResponse(null));
    }
  }
}
