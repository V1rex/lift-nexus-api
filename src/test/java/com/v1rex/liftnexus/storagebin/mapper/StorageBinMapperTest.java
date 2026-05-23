package com.v1rex.liftnexus.storagebin.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import com.v1rex.liftnexus.storagebin.dto.CoordinateDto;
import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StorageBinMapper Unit Tests")
class StorageBinMapperTest {

  private final StorageBinMapper mapper = new StorageBinMapper();

  @Nested
  @DisplayName("Mapping Request to Entity")
  class ToEntityTests {

    @Test
    @DisplayName("Should map valid StorageBinRequest to a complete Entity")
    void shouldMapRequestToEntity() {
      CoordinateDto coordDto = new CoordinateDto(4, 12, 3);
      StorageBinRequest request =
          new StorageBinRequest("A-04-B-12-T-03", coordDto, ZoneType.STORAGE, 1500);

      StorageBin entity = mapper.toEntity(request);

      assertThat(entity).isNotNull();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getBinCode()).isEqualTo("A-04-B-12-T-03");
      assertThat(entity.getCoordinate().getX()).isEqualTo(4);
      assertThat(entity.getCoordinate().getY()).isEqualTo(12);
      assertThat(entity.getCoordinate().getZ()).isEqualTo(3);
      assertThat(entity.getZoneType()).isEqualTo(ZoneType.STORAGE);
      assertThat(entity.getMaxWeightCapacityKg()).isEqualTo(1500);
    }

    @Test
    void shouldReturnNull_WhenRequestIsNull() {
      assertThat(mapper.toEntity(null)).isNull();
    }
  }

  @Nested
  @DisplayName("Mapping Entity to Response")
  class ToResponseTests {

    @Test
    @DisplayName("Should map complete StorageBin entity to nested Response DTO")
    void shouldMapEntityToResponse() {
      StorageBin entity =
          StorageBin.builder()
              .id(42L)
              .binCode("C-01-B-02-T-00")
              .coordinate(new Coordinate3D(1, 2, 0))
              .zoneType(ZoneType.CHARGING_STATION)
              .maxWeightCapacityKg(0)
              .build();

      StorageBinResponse response = mapper.toResponse(entity);

      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(42L);
      assertThat(response.binCode()).isEqualTo("C-01-B-02-T-00");
      assertThat(response.coordinate().x()).isEqualTo(1);
      assertThat(response.coordinate().y()).isEqualTo(2);
      assertThat(response.coordinate().z()).isEqualTo(0);
      assertThat(response.zoneType()).isEqualTo(ZoneType.CHARGING_STATION);
      assertThat(response.maxWeightCapacityKg()).isZero();
    }

    @Test
    void shouldReturnNull_WhenEntityIsNull() {
      assertThat(mapper.toResponse(null)).isNull();
    }
  }
}
