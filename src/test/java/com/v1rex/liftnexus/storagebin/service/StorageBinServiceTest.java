package com.v1rex.liftnexus.storagebin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import com.v1rex.liftnexus.storagebin.dto.CoordinateDto;
import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.exception.StorageBinCodeExistsException;
import com.v1rex.liftnexus.storagebin.exception.StorageBinNotFoundException;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.storagebin.repository.StorageBinRepository;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageBin Service Unit Tests")
public class StorageBinServiceTest {

  @Mock private StorageBinRepository storageBinRepository;
  @Mock private StorageBinMapper storageBinMapper;
  @InjectMocks private StorageBinService storageBinService;

  @Nested
  @DisplayName("Create StorageBin Operations")
  class CreateStorageBin {

    @Test
    @DisplayName("Should save entity and return payload when registration criteria are met")
    void shouldCreateBin_WhenRequestIsValid() {
      // Arrange
      CoordinateDto dtoCoord = new CoordinateDto(1, 2, 3);
      StorageBinRequest request =
          new StorageBinRequest("B-01-02-03", dtoCoord, ZoneType.STORAGE, 1200);

      StorageBin mockEntity = StorageBin.builder().binCode("B-01-02-03").build();

      StorageBin savedEntity = StorageBin.builder().id(100L).binCode("B-01-02-03").build();

      StorageBinResponse expectedResponse =
          new StorageBinResponse(100L, "B-01-02-03", dtoCoord, ZoneType.STORAGE, 1200);

      when(storageBinRepository.existsByBinCode("B-01-02-03")).thenReturn(false);
      when(storageBinMapper.toEntity(request)).thenReturn(mockEntity);
      when(storageBinRepository.save(mockEntity)).thenReturn(savedEntity);
      when(storageBinMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

      // Act
      StorageBinResponse output = storageBinService.createStorageBin(request);

      // Assert
      assertThat(output).isNotNull();
      assertThat(output.id()).isEqualTo(100L);
      assertThat(output.binCode()).isEqualTo("B-01-02-03");
      verify(storageBinRepository, times(1)).save(any(StorageBin.class));
    }

    @Test
    @DisplayName("Should prevent instantiation if a target code collision occurs")
    void shouldThrowException_WhenBinCodeAlreadyExists() {
      // Arrange
      StorageBinRequest request =
          new StorageBinRequest("EXISTS", new CoordinateDto(1, 1, 1), ZoneType.STORAGE, 500);

      // Act
      when(storageBinRepository.existsByBinCode("EXISTS")).thenReturn(true);

      // Assert
      assertThatThrownBy(() -> storageBinService.createStorageBin(request))
          .isInstanceOf(StorageBinCodeExistsException.class);

      verify(storageBinRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Query Isolation Verification")
  class FindStorageBinById {

    @Test
    @DisplayName("Should extract mapped payload cleanly from persistent state records")
    void shouldReturnResponse_WhenStorageBinExists() {
      // Arrange
      Long targetId = 1L;
      StorageBin storedBin = StorageBin.builder().id(targetId).binCode("TEST").build();

      StorageBinResponse responseDto =
          new StorageBinResponse(
              targetId, "TEST", new CoordinateDto(1, 1, 1), ZoneType.STORAGE, 10);

      when(storageBinRepository.findById(targetId)).thenReturn(Optional.of(storedBin));
      when(storageBinMapper.toResponse(storedBin)).thenReturn(responseDto);

      // Act
      StorageBinResponse operationalResult = storageBinService.findById(targetId);

      // Assert
      assertThat(operationalResult).isNotNull();
      verify(storageBinRepository).findById(targetId);
    }

    @Test
    @DisplayName("Should surface structural missing resource exceptions up through operations")
    void shouldThrowException_WhenStorageBinMissing() {
      // Arrange
      Long failingId = 99L;
      when(storageBinRepository.findById(failingId)).thenReturn(Optional.empty());

      // Act and Assert
      assertThatThrownBy(() -> storageBinService.findById(failingId))
          .isInstanceOf(StorageBinNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("Find All StorageBins Operations")
  class FindAllStorageBins {

    @Test
    @DisplayName("Should return a paginated list of StorageBinResponses")
    void shouldReturnPageOfStorageBinResponses() {
      // Arrange
      Pageable pageable = Pageable.unpaged();

      StorageBin entity = StorageBin.builder().id(1L).binCode("A-01").build();

      Page<StorageBin> entityPage = new PageImpl<>(java.util.List.of(entity));

      CoordinateDto dtoCoord = new CoordinateDto(1, 1, 1);
      StorageBinResponse responseDto =
          new StorageBinResponse(1L, "A-01", dtoCoord, ZoneType.STORAGE, 1000);

      when(storageBinRepository.findAll(pageable)).thenReturn(entityPage);
      when(storageBinMapper.toResponse(entity)).thenReturn(responseDto);

      // Act
      Page<StorageBinResponse> result = storageBinService.findAll(pageable);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).binCode()).isEqualTo("A-01");

      verify(storageBinRepository).findAll(pageable);
      verify(storageBinMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Should return a paginated list of underlying entities directly")
    void shouldReturnPageOfStorageBinEntities() {
      // Arrange
      Pageable pageable = Pageable.unpaged();

      StorageBin entity = StorageBin.builder().id(2L).binCode("B-02").build();

      Page<StorageBin> entityPage = new PageImpl<>(java.util.List.of(entity));

      when(storageBinRepository.findAll(pageable)).thenReturn(entityPage);

      // Act
      Page<StorageBin> result = storageBinService.findAllEntities(pageable);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getBinCode()).isEqualTo("B-02");

      verify(storageBinRepository).findAll(pageable);
      verifyNoMoreInteractions(storageBinMapper);
    }
  }
}
