package com.v1rex.liftnexus.forklift.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ForkliftTypeMapperTest {

  private final ForkliftTypeMapper mapper = new ForkliftTypeMapper();

  @Nested
  @DisplayName("Tests - toEntity(ForkliftTypeRequest)")
  class ToEntityTest {

    @Test
    @DisplayName("Should map request to entity successfully")
    void shouldMapToEntity() {
      ForkliftTypeRequest request =
          new ForkliftTypeRequest("Toyota Traigo 48", EquipmentType.STANDARD, 2000, 50.0, 0.5);

      ForkliftType entity = mapper.toEntity(request);

      assertNotNull(entity);
      assertEquals("Toyota Traigo 48", entity.getModelName());
      assertEquals(EquipmentType.STANDARD, entity.getEquipmentType());
      assertEquals(2000, entity.getMaxCapacityKg());
      assertEquals(50.0, entity.getTotalBatteryCapacitykWh());
      assertEquals(0.5, entity.getBaseEnergyConsumptionPerMeter());
    }

    @Test
    @DisplayName("Should return null when request is null")
    void shouldReturnNullWhenRequestIsNull() {
      assertNull(mapper.toEntity(null));
    }
  }

  @Nested
  @DisplayName("Tests - toResponse(ForkliftType)")
  class ToResponseTest {

    @Test
    @DisplayName("Should map entity to response successfully")
    void shouldMapToResponse() {
      ForkliftType entity =
          ForkliftType.builder()
              .id(1L)
              .modelName("Toyota Traigo 48")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(2000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      ForkliftTypeResponse response = mapper.toResponse(entity);

      assertNotNull(response);
      assertEquals(1L, response.id());
      assertEquals("Toyota Traigo 48", response.modelName());
      assertEquals(EquipmentType.STANDARD, response.equipmentType());
      assertEquals(2000, response.maxCapacityKg());
      assertEquals(50.0, response.totalBatteryCapacitykWh());
      assertEquals(0.5, response.baseEnergyConsumptionPerMeter());
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
      assertNull(mapper.toResponse(null));
    }
  }
}
