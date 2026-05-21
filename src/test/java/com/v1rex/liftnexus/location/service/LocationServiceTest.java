package com.v1rex.liftnexus.location.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.location.dto.LocationRequest;
import com.v1rex.liftnexus.location.dto.LocationResponse;
import com.v1rex.liftnexus.location.mapper.LocationMapper;
import com.v1rex.liftnexus.location.repository.LocationRepository;
import java.util.Optional;
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
@DisplayName("Location Service Unit Tests")
public class LocationServiceTest {

  @Mock private LocationRepository locationRepository;

  @Mock private LocationMapper locationMapper;

  @InjectMocks private LocationService locationService;

  @Nested
  @DisplayName("Create Location Feature")
  class CreateLocation {

    @Test
    @DisplayName("Should successfully create a Location")
    void createLocation_ShouldReturnResponse_WhenRequestIsValid() {
      LocationRequest locationRequest = new LocationRequest(40.71F, -74.07F);

      Location location = new Location();
      location.setLatitude(40.71F);
      location.setLongitude(-74.07F);

      when(locationMapper.toEntity(locationRequest)).thenReturn(location);

      Location savedLocation = new Location();
      savedLocation.setId(1L);
      savedLocation.setLatitude(40.71F);
      savedLocation.setLongitude(-74.07F);

      when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

      LocationResponse mockResponse = new LocationResponse(1L, 40.71F, -74.07F);
      when(locationMapper.toResponse(savedLocation)).thenReturn(mockResponse);

      LocationResponse result = locationService.createLocation(locationRequest);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.latitude()).isEqualTo(40.71F);
      assertThat(result.longitude()).isEqualTo(-74.07F);
    }
  }

  @Nested
  @DisplayName("Find Location by ID Feature")
  class FindLocationById {
    private final Long locationId = 1L;

    @Test
    @DisplayName("Should return LocationResponse when location exists")
    void findById_ShouldReturnResponse_WhenLocationExists() {
      Location existingLocation = new Location();
      existingLocation.setId(locationId);
      existingLocation.setLatitude(40.71F);
      existingLocation.setLongitude(-74.07F);

      when(locationRepository.findById(locationId)).thenReturn(Optional.of(existingLocation));

      LocationResponse mockResponse = new LocationResponse(locationId, 40.71F, -74.07F);
      when(locationMapper.toResponse(existingLocation)).thenReturn(mockResponse);

      LocationResponse result = locationService.findById(locationId);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(locationId);
      verify(locationRepository, times(1)).findById(locationId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when location does not exist")
    void findById_ShouldThrowException_WhenLocationDoesNotExist() {
      when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> locationService.findById(locationId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Location  with " + locationId + " not found.");

      verifyNoInteractions(locationMapper);
    }
  }

  @Nested
  @DisplayName("Find all Locations Feature")
  class FindAllLocations {
    @Test
    @DisplayName("Should return paginated LocationResponses")
    void findAll_ShouldReturnPage_WhenCalled() {
      Pageable pageable = Pageable.unpaged();
      Location location = new Location();
      location.setId(1L);
      Page<Location> mockPage =
          new org.springframework.data.domain.PageImpl<>(java.util.List.of(location));

      when(locationRepository.findAll(pageable)).thenReturn(mockPage);

      LocationResponse mockResponse = new LocationResponse(1L, 40.71F, -74.07F);
      when(locationMapper.toResponse(location)).thenReturn(mockResponse);

      Page<LocationResponse> result = locationService.findAll(pageable);

      assertThat(result).isNotNull();
      assertThat(result.getContent().get(0).id()).isEqualTo(1L);
      verify(locationRepository, times(1)).findAll(pageable);
    }
  }
}
