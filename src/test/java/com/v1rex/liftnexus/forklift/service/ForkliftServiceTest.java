/*
package com.v1rex.liftnexus.forklift.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.forklift.mapper.ForkliftMapper;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
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
@DisplayName("Forklift Service Unit Tests")
public class ForkliftServiceTest {
  @Mock private ForkliftRepository forkliftRepository;

  @Mock private ForkliftMapper forkliftMapper;

  @Mock private StorageBinService storageBinService;

  @InjectMocks private ForkliftService forkliftService;

  @Nested
  @DisplayName("Create Forklift Feature")
  class CreateForklift {

    @Test
    @DisplayName("Should successfully create a Forklift")
    void createForklift_ShouldReturnResponse_WhenRequestIsValid() {
      ForkliftRequest forkliftRequest = new ForkliftRequest(1000, EquipmentType.STANDARD);

      Forklift forklift = new Forklift();
      forklift.setWeightCapacity(1000);
      forklift.setEquipmentType(EquipmentType.STANDARD);

      when(forkliftMapper.toEntity(forkliftRequest)).thenReturn(forklift);

      Forklift savedForklift = new Forklift();
      savedForklift.setId(1L);
      savedForklift.setWeightCapacity(1000);
      savedForklift.setEquipmentType(EquipmentType.STANDARD);

      when(forkliftRepository.save(any(Forklift.class))).thenReturn(savedForklift);

      ForkliftResponse mockResponse =
          new ForkliftResponse(1L, 1000, EquipmentType.STANDARD, null, null);

      when(forkliftMapper.toResponse(savedForklift)).thenReturn(mockResponse);

      ForkliftResponse result = forkliftService.createForklift(forkliftRequest);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.weightCapacity()).isEqualTo(1000);
      assertThat(result.equipmentType()).isEqualTo(EquipmentType.STANDARD);

      verify(forkliftRepository, times(1)).save(any(Forklift.class));
    }
  }

  @Nested
  @DisplayName("Find Forklift by ID Feature")
  class FindForkliftById {
    private final Long forkliftId = 1L;

    @Test
    @DisplayName("Should return ForkliftResponse when forklift exists")
    void findById_ShouldReturnResponse_WhenForkliftExists() {
      Forklift existingForklift = new Forklift();
      existingForklift.setId(forkliftId);
      existingForklift.setWeightCapacity(1000);
      existingForklift.setEquipmentType(EquipmentType.STANDARD);

      when(forkliftRepository.findById(forkliftId)).thenReturn(Optional.of(existingForklift));

      ForkliftResponse mockResponse =
          new ForkliftResponse(forkliftId, 1000, EquipmentType.STANDARD, null, null);

      when(forkliftMapper.toResponse(existingForklift)).thenReturn(mockResponse);

      ForkliftResponse result = forkliftService.findById(forkliftId);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(forkliftId);
      verify(forkliftRepository, times(1)).findById(forkliftId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when forklift does not exist")
    void findById_ShouldThrowException_WhenForkliftDoesNotExist() {
      when(forkliftRepository.findById(forkliftId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> forkliftService.findById(forkliftId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Forklift with " + forkliftId + " not found.");

      verifyNoInteractions(forkliftMapper);
    }
  }

  @Nested
  @DisplayName("Find all Forklifts Feature")
  class FindAllForklifts {
    @Test
    @DisplayName("Should return paginated ForkliftResponses")
    void findAll_ShouldReturnPage_WhenCalled() {
      Pageable pageable = Pageable.unpaged();
      Forklift forklift = new Forklift();
      forklift.setId(1L);
      forklift.setWeightCapacity(1000);
      forklift.setEquipmentType(EquipmentType.STANDARD);

      Page<Forklift> mockPage =
          new org.springframework.data.domain.PageImpl<>(java.util.List.of(forklift));

      when(forkliftRepository.findAll(pageable)).thenReturn(mockPage);

      ForkliftResponse mockResponse =
          new ForkliftResponse(1L, 1000, EquipmentType.STANDARD, null, null);

      when(forkliftMapper.toResponse(forklift)).thenReturn(mockResponse);

      Page<ForkliftResponse> result = forkliftService.findAll(pageable);

      assertThat(result).isNotNull();
      assertThat(result.getContent().get(0).id()).isEqualTo(1L);
      verify(forkliftRepository, times(1)).findAll(pageable);
    }
  }

  @Nested
  @DisplayName("Find Forklifts with Capacity Greater Than Feature")
  class FindForkliftsWithCapacityGreaterThan {
    @Test
    @DisplayName("Should return paginated ForkliftResponses with capacity greater than threshold")
    void findWithCapacityGreaterThan_ShouldReturnPage_WhenCriteriaIsValid() {
      Integer weightCapacity = 800;
      Pageable pageable = Pageable.unpaged();

      Forklift forklift1 = new Forklift();
      forklift1.setId(1L);
      forklift1.setWeightCapacity(1000);
      forklift1.setEquipmentType(EquipmentType.STANDARD);

      Forklift forklift2 = new Forklift();
      forklift2.setId(2L);
      forklift2.setWeightCapacity(1500);
      forklift2.setEquipmentType(EquipmentType.REACH_TRUCK);

      Page<Forklift> mockPage =
          new org.springframework.data.domain.PageImpl<>(java.util.List.of(forklift1, forklift2));

      when(forkliftRepository.findByWeightCapacityGreaterThan(weightCapacity, pageable))
          .thenReturn(mockPage);

      ForkliftResponse response1 =
          new ForkliftResponse(1L, 1000, EquipmentType.STANDARD, null, null);

      ForkliftResponse response2 =
          new ForkliftResponse(2L, 1500, EquipmentType.REACH_TRUCK, null, null);

      when(forkliftMapper.toResponse(forklift1)).thenReturn(response1);
      when(forkliftMapper.toResponse(forklift2)).thenReturn(response2);

      Page<ForkliftResponse> result =
          forkliftService.findWithCapacityGreaterThan(weightCapacity, pageable);

      assertThat(result).isNotNull();
      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getContent().get(0).weightCapacity()).isGreaterThan(weightCapacity);
      assertThat(result.getContent().get(1).weightCapacity()).isGreaterThan(weightCapacity);

      verify(forkliftRepository, times(1))
          .findByWeightCapacityGreaterThan(weightCapacity, pageable);
    }
  }

  @Nested
  @DisplayName("Update Forklift StorageBin Feature")
  class UpdateForkliftStorageBin {
    private final Long forkliftId = 1L;
    private final Long locationId = 10L;

    @Test
    @DisplayName(
        "Should successfully update forklift storagebin when both forklift and storagebin exist")
    void updateForkliftLocation_ShouldReturnResponse_WhenBothExist() {
      Forklift existingForklift = new Forklift();
      existingForklift.setId(forkliftId);
      existingForklift.setWeightCapacity(1000);
      existingForklift.setEquipmentType(EquipmentType.STANDARD);

      StorageBin newStorageBin = new StorageBin();
      newStorageBin.setId(locationId);
      newStorageBin.setLatitude(40.71F);
      newStorageBin.setLongitude(-74.07F);

      when(forkliftRepository.findById(forkliftId)).thenReturn(Optional.of(existingForklift));
      when(storageBinService.findEntityById(locationId)).thenReturn(newStorageBin);

      Forklift updatedForklift = new Forklift();
      updatedForklift.setId(forkliftId);
      updatedForklift.setWeightCapacity(1000);
      updatedForklift.setEquipmentType(EquipmentType.STANDARD);
      updatedForklift.setCurrentStorageBin(newStorageBin);

      when(forkliftRepository.save(any(Forklift.class))).thenReturn(updatedForklift);

      StorageBinResponse storageBinResponse = new StorageBinResponse(locationId, 40.71F, -74.07F);

      ForkliftResponse mockResponse =
          new ForkliftResponse(forkliftId, 1000, EquipmentType.STANDARD, null, storageBinResponse);

      when(forkliftMapper.toResponse(updatedForklift)).thenReturn(mockResponse);

      ForkliftResponse result = forkliftService.updateForkliftLocation(forkliftId, locationId);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(forkliftId);
      assertThat(result.currentLocation()).isEqualTo(storageBinResponse);
      verify(forkliftRepository, times(1)).save(any(Forklift.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when forklift does not exist")
    void updateForkliftLocation_ShouldThrowException_WhenForkliftDoesNotExist() {
      when(forkliftRepository.findById(forkliftId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> forkliftService.updateForkliftLocation(forkliftId, locationId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Forklift with " + forkliftId + " not found.");

      verifyNoInteractions(storageBinService);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when storagebin does not exist")
    void updateForkliftLocation_ShouldThrowException_WhenLocationDoesNotExist() {
      Forklift existingForklift = new Forklift();
      existingForklift.setId(forkliftId);

      when(forkliftRepository.findById(forkliftId)).thenReturn(Optional.of(existingForklift));
      when(storageBinService.findEntityById(locationId))
          .thenThrow(new ResourceNotFoundException("StorageBin with " + locationId + " not found."));

      assertThatThrownBy(() -> forkliftService.updateForkliftLocation(forkliftId, locationId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("StorageBin with " + locationId + " not found.");

      verify(forkliftRepository, never()).save(any());
    }
  }
}
*/
