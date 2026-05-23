package com.v1rex.liftnexus.loadunit.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoadUnitMapper Unit Tests")
class LoadUnitMapperTest {

  private final LoadUnitMapper mapper = new LoadUnitMapper();

  @Test
  @DisplayName("Should map complete Request payload to Entity structure cleanly")
  void shouldMapRequestToEntity() {
    LoadUnitRequest request = new LoadUnitRequest("LU-MAPPED-1", 450, LoadUnitStatus.EXPECTED, 10L);
    StorageBin mockBin = StorageBin.builder().id(10L).binCode("BIN-A-01").build();

    LoadUnit result = mapper.toEntity(request, mockBin);

    assertThat(result).isNotNull();
    assertThat(result.getTrackingCode()).isEqualTo("LU-MAPPED-1");
    assertThat(result.getWeightKg()).isEqualTo(450);
    assertThat(result.getStatus()).isEqualTo(LoadUnitStatus.EXPECTED);
    assertThat(result.getCurrentBin()).isEqualTo(mockBin);
    assertThat(result.getId()).isNull();
  }

  @Test
  @DisplayName("Should map entity instance fields to lean flat Response record payload")
  void shouldMapEntityToResponse() {
    StorageBin mockBin = StorageBin.builder().id(25L).binCode("BIN-C-05").build();
    LoadUnit entity =
        LoadUnit.builder()
            .id(1L)
            .trackingCode("LU-ENTITY-1")
            .weightKg(1200)
            .status(LoadUnitStatus.STORED)
            .currentBin(mockBin)
            .version(3L)
            .build();

    LoadUnitResponse response = mapper.toResponse(entity);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.trackingCode()).isEqualTo("LU-ENTITY-1");
    assertThat(response.weightKg()).isEqualTo(1200);
    assertThat(response.status()).isEqualTo(LoadUnitStatus.STORED);
    assertThat(response.currentStorageBinId()).isEqualTo(25L);
    assertThat(response.version()).isEqualTo(3L);
  }

  @Test
  @DisplayName(
      "Should gracefully map null inputs or null references without throwing NullPointerExceptions")
  void shouldHandleNullReferencesGracefully() {
    assertThat(mapper.toEntity(null, null)).isNull();
    assertThat(mapper.toResponse(null)).isNull();

    LoadUnit partialEntity =
        LoadUnit.builder()
            .id(5L)
            .trackingCode("LU-ORPHAN")
            .weightKg(10)
            .status(LoadUnitStatus.EXPECTED)
            .currentBin(null) // Not placed in a physical grid yet
            .version(0L)
            .build();

    LoadUnitResponse response = mapper.toResponse(partialEntity);
    assertThat(response.currentStorageBinId()).isNull();
  }
}
