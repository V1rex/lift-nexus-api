package com.v1rex.liftnexus.storagebin.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.LocationRequest;
import com.v1rex.liftnexus.storagebin.dto.LocationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StorageBinMapperTest {

  private final LocationMapper mapper = new LocationMapper();

  @Nested
  @DisplayName("Tests for toEntity mapping")
  class ToEntityTests {

    @Test
    @DisplayName("Should correctly map LocationRequest to StorageBin Entity")
    void shouldMapRequestToEntity() {

      LocationRequest request = new LocationRequest(51.5136F, 7.4653F);

      StorageBin entity = mapper.toEntity(request);

      assertNotNull(entity);
      assertNull(entity.getId(), "New entities mapped from a request should not have an ID yet");
      assertEquals(51.5136F, entity.getLatitude());
      assertEquals(7.4653F, entity.getLongitude());
    }

    @Test
    @DisplayName("Should return null when LocationRequest is null")
    void shouldReturnNull_WhenRequestIsNull() {
      StorageBin entity = mapper.toEntity(null);

      assertNull(entity);
    }
  }

  @Nested
  @DisplayName("Tests for toResponse mapping")
  class ToResponseTests {

    @Test
    @DisplayName("Should correctly map StorageBin Entity to LocationResponse DTO")
    void shouldMapEntityToResponse() {
      StorageBin entity = StorageBin.builder().id(42L).latitude(51.5136F).longitude(7.4653F).build();

      LocationResponse response = mapper.toResponse(entity);

      assertNotNull(response);
      assertEquals(42L, response.id());
      assertEquals(51.5136F, response.latitude());
      assertEquals(7.4653F, response.longitude());
    }

    @Test
    @DisplayName("Should return null when StorageBin Entity is null")
    void shouldReturnNull_WhenEntityIsNull() {
      LocationResponse response = mapper.toResponse(null);

      assertNull(response);
    }
  }
}
