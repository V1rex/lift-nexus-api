package com.v1rex.liftnexus.storagebin.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.LocationRequest;
import com.v1rex.liftnexus.storagebin.dto.LocationResponse;
import com.v1rex.liftnexus.storagebin.mapper.LocationMapper;
import com.v1rex.liftnexus.storagebin.repository.LocationRepository;
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
@DisplayName("StorageBin Service Unit Tests")
public class StorageBinServiceTest {

  @Mock private LocationRepository locationRepository;

  @Mock private LocationMapper locationMapper;

  @InjectMocks private LocationService locationService;

  @Nested
  @DisplayName("Create StorageBin Feature")
  class CreateStorageBin {

    @Test
    @DisplayName("Should successfully create a StorageBin")
    void createLocation_ShouldReturnResponse_WhenRequestIsValid() {
      LocationRequest locationRequest = new LocationRequest(40.71F, -74.07F);

      StorageBin storageBin = new StorageBin();
      storageBin.setLatitude(40.71F);
      storageBin.setLongitude(-74.07F);

      when(locationMapper.toEntity(locationRequest)).thenReturn(storageBin);

      StorageBin savedStorageBin = new StorageBin();
      savedStorageBin.setId(1L);
      savedStorageBin.setLatitude(40.71F);
      savedStorageBin.setLongitude(-74.07F);

      when(locationRepository.save(any(StorageBin.class))).thenReturn(savedStorageBin);

      LocationResponse mockResponse = new LocationResponse(1L, 40.71F, -74.07F);
      when(locationMapper.toResponse(savedStorageBin)).thenReturn(mockResponse);

      LocationResponse result = locationService.createLocation(locationRequest);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.latitude()).isEqualTo(40.71F);
      assertThat(result.longitude()).isEqualTo(-74.07F);
    }
  }

  @Nested
  @DisplayName("Find StorageBin by ID Feature")
  class FindStorageBinById {
    private final Long locationId = 1L;

    @Test
    @DisplayName("Should return LocationResponse when storagebin exists")
    void findById_ShouldReturnResponse_WhenLocationExists() {
      StorageBin existingStorageBin = new StorageBin();
      existingStorageBin.setId(locationId);
      existingStorageBin.setLatitude(40.71F);
      existingStorageBin.setLongitude(-74.07F);

      when(locationRepository.findById(locationId)).thenReturn(Optional.of(existingStorageBin));

      LocationResponse mockResponse = new LocationResponse(locationId, 40.71F, -74.07F);
      when(locationMapper.toResponse(existingStorageBin)).thenReturn(mockResponse);

      LocationResponse result = locationService.findById(locationId);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(locationId);
      verify(locationRepository, times(1)).findById(locationId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when storagebin does not exist")
    void findById_ShouldThrowException_WhenLocationDoesNotExist() {
      when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> locationService.findById(locationId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("StorageBin  with " + locationId + " not found.");

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
      StorageBin storageBin = new StorageBin();
      storageBin.setId(1L);
      Page<StorageBin> mockPage =
          new org.springframework.data.domain.PageImpl<>(java.util.List.of(storageBin));

      when(locationRepository.findAll(pageable)).thenReturn(mockPage);

      LocationResponse mockResponse = new LocationResponse(1L, 40.71F, -74.07F);
      when(locationMapper.toResponse(storageBin)).thenReturn(mockResponse);

      Page<LocationResponse> result = locationService.findAll(pageable);

      assertThat(result).isNotNull();
      assertThat(result.getContent().get(0).id()).isEqualTo(1L);
      verify(locationRepository, times(1)).findAll(pageable);
    }
  }
}
