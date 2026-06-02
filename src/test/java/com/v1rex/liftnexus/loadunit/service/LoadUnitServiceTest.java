package com.v1rex.liftnexus.loadunit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
import com.v1rex.liftnexus.loadunit.exception.LoadUnitNotFoundException;
import com.v1rex.liftnexus.loadunit.exception.LoadUnitTrackingCodeExistsException;
import com.v1rex.liftnexus.loadunit.mapper.LoadUnitMapper;
import com.v1rex.liftnexus.loadunit.repository.LoadUnitRepository;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoadUnitService Business Logic Tests")
class LoadUnitServiceTest {

  @Mock private LoadUnitRepository loadUnitRepository;
  @Mock private LoadUnitMapper loadUnitMapper;
  @Mock private StorageBinService storageBinService;

  @InjectMocks private LoadUnitService loadUnitService;

  @Nested
  @DisplayName("Feature: Create LoadUnit Workflow")
  class CreateLoadUnit {

    @Test
    @DisplayName(
        "Should successfully create load unit when payload constraints are valid and distinct")
    void shouldCreateLoadUnitSuccessfully() {
      // Arrange
      LoadUnitRequest request = new LoadUnitRequest("LU-NEW", 500, LoadUnitStatus.STAGED, 1L);

      StorageBin resolvedBin = StorageBin.builder().id(1L).build();
      LoadUnit mockEntity = new LoadUnit();
      LoadUnit savedEntity = new LoadUnit();
      LoadUnitResponse expectedResponse =
          new LoadUnitResponse(100L, "LU-NEW", 500, LoadUnitStatus.STAGED, 1L, 0L);

      when(loadUnitRepository.existsByTrackingCode("LU-NEW")).thenReturn(false);
      when(storageBinService.findEntityById(1L)).thenReturn(resolvedBin);
      when(loadUnitMapper.toEntity(request, resolvedBin)).thenReturn(mockEntity);
      when(loadUnitRepository.save(mockEntity)).thenReturn(savedEntity);
      when(loadUnitMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

      // Act
      LoadUnitResponse operationalResult = loadUnitService.createLoadUnit(request);

      // Assert
      assertThat(operationalResult).isEqualTo(expectedResponse);
      verify(loadUnitRepository).save(mockEntity);
    }

    @Test
    @DisplayName(
        "Should abort creation and throw LoadUnitTrackingCodeExistsException on business-key code duplication")
    void shouldThrowExceptionOnDuplicateTrackingCode() {
      // Arrange
      LoadUnitRequest request =
          new LoadUnitRequest("LU-CONFLICT", 200, LoadUnitStatus.EXPECTED, null);

      when(loadUnitRepository.existsByTrackingCode("LU-CONFLICT")).thenReturn(true);

      // Act and Assert
      assertThatThrownBy(() -> loadUnitService.createLoadUnit(request))
          .isInstanceOf(LoadUnitTrackingCodeExistsException.class);

      verify(loadUnitRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Feature: Retrieve Single LoadUnit Entity Context")
  class FindSingleUnit {

    @Test
    @DisplayName(
        "Should map entity directly to Response record if technical internal identifier exists")
    void shouldFindByIdWhenExists() {
      // Arrange
      LoadUnit entity = LoadUnit.builder().id(1L).trackingCode("LU-1").build();
      LoadUnitResponse response =
          new LoadUnitResponse(1L, "LU-1", 100, LoadUnitStatus.EXPECTED, null, 0L);

      when(loadUnitRepository.findById(1L)).thenReturn(Optional.of(entity));
      when(loadUnitMapper.toResponse(entity)).thenReturn(response);

      // Act
      LoadUnitResponse result = loadUnitService.findById(1L);

      // Assert
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName(
        "Should capture failure and throw LoadUnitNotFoundException if technical ID does not match any entry")
    void shouldThrowNotFoundOnMissingId() {
      // Arrange
      when(loadUnitRepository.findById(99L)).thenReturn(Optional.empty());

      // Act and Assert
      assertThatThrownBy(() -> loadUnitService.findById(99L))
          .isInstanceOf(LoadUnitNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("Feature: Paginated Retrieval Filter Loops")
  class PaginatedQueries {

    @Test
    @DisplayName("Should route clean execution data straight to mapped status pages")
    void shouldFilterByStatusCorrectly() {
      // Arrange
      Pageable pageable = PageRequest.of(0, 10);
      LoadUnit unit =
          LoadUnit.builder().trackingCode("LU-ACTIVE").status(LoadUnitStatus.IN_TRANSIT).build();
      Page<LoadUnit> entityPage = new PageImpl<>(List.of(unit));
      LoadUnitResponse mappedResponse =
          new LoadUnitResponse(2L, "LU-ACTIVE", 400, LoadUnitStatus.IN_TRANSIT, null, 0L);

      when(loadUnitRepository.findByStatus(LoadUnitStatus.IN_TRANSIT, pageable))
          .thenReturn(entityPage);
      when(loadUnitMapper.toResponse(unit)).thenReturn(mappedResponse);

      // Act
      Page<LoadUnitResponse> finalPage =
          loadUnitService.findByStatus(LoadUnitStatus.IN_TRANSIT, pageable);

      // Assert
      assertThat(finalPage.getContent()).hasSize(1);
      assertThat(finalPage.getContent().get(0)).isEqualTo(mappedResponse);
    }
  }
}
