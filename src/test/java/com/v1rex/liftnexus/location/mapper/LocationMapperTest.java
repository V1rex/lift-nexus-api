package com.v1rex.liftnexus.location.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.location.dto.LocationRequest;
import com.v1rex.liftnexus.location.dto.LocationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LocationMapperTest {

  private final LocationMapper mapper = new LocationMapper();

  @Nested
  @DisplayName("Tests for toEntity mapping")
  class ToEntityTests {

    @Test
    @DisplayName("Should correctly map LocationRequest to Location Entity")
    void shouldMapRequestToEntity() {

      LocationRequest request = new LocationRequest(51.5136F, 7.4653F);

      Location entity = mapper.toEntity(request);

      assertNotNull(entity);
      assertNull(entity.getId(), "New entities mapped from a request should not have an ID yet");
      assertEquals(51.5136F, entity.getLatitude());
      assertEquals(7.4653F, entity.getLongitude());
    }

    @Test
    @DisplayName("Should return null when LocationRequest is null")
    void shouldReturnNull_WhenRequestIsNull() {
      Location entity = mapper.toEntity(null);

      assertNull(entity);
    }
  }

  @Nested
  @DisplayName("Tests for toResponse mapping")
  class ToResponseTests {

    @Test
    @DisplayName("Should correctly map Location Entity to LocationResponse DTO")
    void shouldMapEntityToResponse() {
      Location entity = Location.builder().id(42L).latitude(51.5136F).longitude(7.4653F).build();

      LocationResponse response = mapper.toResponse(entity);

      assertNotNull(response);
      assertEquals(42L, response.id());
      assertEquals(51.5136F, response.latitude());
      assertEquals(7.4653F, response.longitude());
    }

    @Test
    @DisplayName("Should return null when Location Entity is null")
    void shouldReturnNull_WhenEntityIsNull() {
      LocationResponse response = mapper.toResponse(null);

      assertNull(response);
    }
  }
}
