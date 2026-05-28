package com.v1rex.liftnexus.forklift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.forklift.mapper.ForkliftMapper;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
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
public class ForkliftServiceTest {

  @Mock private ForkliftRepository forkliftRepository;
  @Mock private ForkliftTypeService forkliftTypeService;
  @Mock private StorageBinService storageBinService;
  @Mock private ForkliftMapper forkliftMapper;

  @InjectMocks private ForkliftService forkliftService;

  @Nested
  @DisplayName("Tests - createForklift()")
  class CreateForkliftTests {

    @Test
    @DisplayName("Should provision a new forklift successfully with a storage bin")
    void shouldCreateForkliftWithStorageBin() {
      ForkliftRequest request =
          new ForkliftRequest("FL-01", 1L, 2L, OperationalStatus.ACTIVE, 100.0);

      ForkliftType mockType = new ForkliftType();
      StorageBin mockBin = new StorageBin();

      Forklift mappedEntity = new Forklift();
      Forklift savedEntity = new Forklift();
      savedEntity.setId(99L);

      ForkliftResponse expectedResponse =
          new ForkliftResponse(
              99L, "FL-01", 1L, "Model", null, 1000, 2L, OperationalStatus.ACTIVE, 100.0, null);

      when(forkliftRepository.existsByFleetNumber("FL-01")).thenReturn(false);

      when(forkliftTypeService.findEntityById(1L)).thenReturn(mockType);

      when(storageBinService.findEntityById(2L)).thenReturn(mockBin);

      when(forkliftMapper.toEntity(request)).thenReturn(mappedEntity);

      when(forkliftRepository.save(mappedEntity)).thenReturn(savedEntity);

      when(forkliftMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

      ForkliftResponse actualResponse = forkliftService.createForklift(request);

      assertThat(actualResponse.id()).isEqualTo(99L);
      assertThat(mappedEntity.getForkliftType()).isEqualTo(mockType);
      assertThat(mappedEntity.getCurrentStorageBin()).isEqualTo(mockBin);
    }

    @Test
    @DisplayName("Should provision a new forklift successfully without a storage bin (null check)")
    void shouldCreateForkliftWithoutStorageBin() {
      ForkliftRequest request =
          new ForkliftRequest("FL-02", 1L, null, OperationalStatus.ACTIVE, 100.0);

      ForkliftType mockType = new ForkliftType();

      Forklift mappedEntity = new Forklift();
      Forklift savedEntity = new Forklift();
      savedEntity.setId(100L);

      ForkliftResponse expectedResponse =
          new ForkliftResponse(
              100L, "FL-02", 1L, "Model", null, 1000, null, OperationalStatus.ACTIVE, 100.0, null);

      when(forkliftRepository.existsByFleetNumber("FL-02")).thenReturn(false);
      when(forkliftTypeService.findEntityById(1L)).thenReturn(mockType);
      when(forkliftMapper.toEntity(request)).thenReturn(mappedEntity);
      when(forkliftRepository.save(mappedEntity)).thenReturn(savedEntity);
      when(forkliftMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

      ForkliftResponse actualResponse = forkliftService.createForklift(request);

      assertThat(actualResponse.id()).isEqualTo(100L);
      assertThat(mappedEntity.getForkliftType()).isEqualTo(mockType);
      assertThat(mappedEntity.getCurrentStorageBin()).isNull();
      verifyNoInteractions(storageBinService);
    }

    @Test
    @DisplayName("Should throw IllegalStateException if fleet number exists")
    void shouldThrowIfFleetNumberExists() {
      ForkliftRequest request = new ForkliftRequest("FL-DUP", 1L, null, null, null);
      when(forkliftRepository.existsByFleetNumber("FL-DUP")).thenReturn(true);

      assertThatThrownBy(() -> forkliftService.createForklift(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("already exists");

      verifyNoInteractions(forkliftTypeService, storageBinService, forkliftMapper);
    }
  }

  @Nested
  @DisplayName("Tests - findById()")
  class FindByIdTests {

    @Test
    @DisplayName("Should find response by ID")
    void shouldFindById() {
      Forklift entity = new Forklift();
      entity.setId(1L);
      ForkliftResponse expectedResponse =
          new ForkliftResponse(1L, "FL-01", null, null, null, null, null, null, null, null);

      when(forkliftRepository.findById(1L)).thenReturn(Optional.of(entity));
      when(forkliftMapper.toResponse(entity)).thenReturn(expectedResponse);

      ForkliftResponse response = forkliftService.findById(1L);

      assertThat(response).isEqualTo(expectedResponse);
    }
  }

  @Nested
  @DisplayName("Tests - Retrieval & Pagination Methods")
  class RetrievalTests {

    private final Pageable pageable = PageRequest.of(0, 10);
    private final Forklift entity = new Forklift();
    private final ForkliftResponse responseDto =
        new ForkliftResponse(1L, "FL-01", null, null, null, null, null, null, null, null);

    @Test
    @DisplayName("Should return all forklifts paginated")
    void shouldFindAll() {
      Page<Forklift> page = new PageImpl<>(List.of(entity));

      when(forkliftRepository.findAll(pageable)).thenReturn(page);
      when(forkliftMapper.toResponse(entity)).thenReturn(responseDto);

      Page<ForkliftResponse> result = forkliftService.findAll(pageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0)).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("Should return forklifts filtered by capacity")
    void shouldFindWithCapacityGreaterThan() {
      Page<Forklift> page = new PageImpl<>(List.of(entity));

      when(forkliftRepository.findByForkliftType_MaxCapacityKgGreaterThanEqual(2000, pageable))
          .thenReturn(page);
      when(forkliftMapper.toResponse(entity)).thenReturn(responseDto);

      Page<ForkliftResponse> result = forkliftService.findWithCapacityGreaterThan(2000, pageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0)).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("Should return forklifts filtered by status")
    void shouldFindByStatus() {
      Page<Forklift> page = new PageImpl<>(List.of(entity));

      when(forkliftRepository.findByStatus(OperationalStatus.ACTIVE, pageable)).thenReturn(page);
      when(forkliftMapper.toResponse(entity)).thenReturn(responseDto);

      Page<ForkliftResponse> result =
          forkliftService.findByStatus(OperationalStatus.ACTIVE, pageable);

      assertThat(result.getContent()).hasSize(1);

      assertThat(result.getContent().get(0)).isEqualTo(responseDto);
    }
  }

  @Nested
  @DisplayName("Tests - State Transitions (Updates)")
  class UpdateTests {

    @Test
    @DisplayName("Should update forklift location and save")
    void shouldUpdateLocation() {
      Forklift forklift = new Forklift();
      StorageBin newBin = new StorageBin();
      ForkliftResponse responseDto =
          new ForkliftResponse(1L, null, null, null, null, null, 5L, null, null, null);

      when(forkliftRepository.findById(1L)).thenReturn(Optional.of(forklift));
      when(storageBinService.findEntityById(5L)).thenReturn(newBin);
      when(forkliftRepository.save(forklift)).thenReturn(forklift);
      when(forkliftMapper.toResponse(forklift)).thenReturn(responseDto);

      ForkliftResponse result = forkliftService.updateForkliftLocation(1L, 5L);

      assertThat(forklift.getCurrentStorageBin()).isEqualTo(newBin);
      assertThat(result).isEqualTo(responseDto);
      verify(forkliftRepository).save(forklift);
    }

    @Test
    @DisplayName("Should update operational status and save")
    void shouldUpdateOperationalStatus() {
      Forklift forklift = new Forklift();
      ForkliftResponse responseDto =
          new ForkliftResponse(
              1L, null, null, null, null, null, null, OperationalStatus.MAINTENANCE, null, null);

      when(forkliftRepository.findById(1L)).thenReturn(Optional.of(forklift));

      when(forkliftRepository.save(forklift)).thenReturn(forklift);

      when(forkliftMapper.toResponse(forklift)).thenReturn(responseDto);

      ForkliftResponse result =
          forkliftService.updateOperationalStatus(1L, OperationalStatus.MAINTENANCE);

      assertThat(forklift.getStatus()).isEqualTo(OperationalStatus.MAINTENANCE);
      assertThat(result).isEqualTo(responseDto);
      verify(forkliftRepository).save(forklift);
    }
  }

  @Nested
  @DisplayName("Tests - findEntityById()")
  class FindEntityByIdTests {

    @Test
    @DisplayName("Should return entity if found")
    void shouldReturnEntity() {
      Forklift forklift = new Forklift();
      when(forkliftRepository.findById(1L)).thenReturn(Optional.of(forklift));

      Forklift result = forkliftService.findEntityById(1L);

      assertThat(result).isEqualTo(forklift);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if forklift not found")
    void shouldThrowIfForkliftNotFound() {
      when(forkliftRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> forkliftService.findEntityById(99L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Forklift with 99 not found.");
    }
  }

  @Nested
  @DisplayName("Tests - findAllEntitiesForPlanning()")
  class FindAllEntitiesForPlanningTests {

    @Test
    @DisplayName("Should return raw list of entities for solver")
    void shouldReturnEntityList() {
      Forklift forklift1 = new Forklift();
      Forklift forklift2 = new Forklift();
      List<Forklift> mockList = List.of(forklift1, forklift2);

      when(forkliftRepository.findAll()).thenReturn(mockList);

      List<Forklift> result = forkliftService.findAllEntities();

      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(forklift1, forklift2);
    }
  }
}
