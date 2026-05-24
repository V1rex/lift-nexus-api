package com.v1rex.liftnexus.forklift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import com.v1rex.liftnexus.forklift.mapper.ForkliftTypeMapper;
import com.v1rex.liftnexus.forklift.repository.ForkliftTypeRepository;
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

@ExtendWith(MockitoExtension.class)
public class ForkliftTypeServiceTest {

  @Mock private ForkliftTypeRepository forkliftTypeRepository;
  @Mock private ForkliftTypeMapper forkliftTypeMapper;

  @InjectMocks private ForkliftTypeService forkliftTypeService;

  @Nested
  @DisplayName("Tests - createForkliftType()")
  class CreateForkliftTypeTests {

    @Test
    @DisplayName("Should create and return new blueprint")
    void shouldCreateForkliftType() {
      ForkliftTypeRequest request =
          new ForkliftTypeRequest("Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);

      ForkliftType mappedEntity = new ForkliftType();
      ForkliftType savedEntity = new ForkliftType();
      savedEntity.setId(1L);
      ForkliftTypeResponse expectedResponse =
          new ForkliftTypeResponse(1L, "Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);

      when(forkliftTypeRepository.existsByModelName("Toyota X")).thenReturn(false);
      when(forkliftTypeMapper.toEntity(request)).thenReturn(mappedEntity);
      when(forkliftTypeRepository.save(mappedEntity)).thenReturn(savedEntity);
      when(forkliftTypeMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

      ForkliftTypeResponse actualResponse = forkliftTypeService.createForkliftType(request);

      assertThat(actualResponse).isEqualTo(expectedResponse);
      // State based verify because mappedEntity is a real object
      verify(forkliftTypeRepository).save(mappedEntity);
    }

    @Test
    @DisplayName("Should throw exception if model name already exists")
    void shouldThrowIfModelNameExists() {
      ForkliftTypeRequest request =
          new ForkliftTypeRequest("Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);
      when(forkliftTypeRepository.existsByModelName("Toyota X")).thenReturn(true);

      assertThatThrownBy(() -> forkliftTypeService.createForkliftType(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("already exists");

      verifyNoInteractions(forkliftTypeMapper);
      verify(forkliftTypeRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Tests - findEntityById() & findById()")
  class FindByIdTests {

    @Test
    @DisplayName("Should return entity when ID exists")
    void shouldReturnEntity() {
      ForkliftType entity = new ForkliftType();
      entity.setId(1L);
      when(forkliftTypeRepository.findById(1L)).thenReturn(Optional.of(entity));

      ForkliftType result = forkliftTypeService.findEntityById(1L);

      assertThat(result).isEqualTo(entity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
    void shouldThrowWhenNotFound() {
      when(forkliftTypeRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> forkliftTypeService.findEntityById(99L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("ForkliftType with ID 99 not found.");
    }

    @Test
    @DisplayName("Should return response DTO when ID exists")
    void shouldReturnResponseDto() {
      ForkliftType entity = new ForkliftType();
      ForkliftTypeResponse response =
          new ForkliftTypeResponse(1L, "Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);

      when(forkliftTypeRepository.findById(1L)).thenReturn(Optional.of(entity));
      when(forkliftTypeMapper.toResponse(entity)).thenReturn(response);

      ForkliftTypeResponse result = forkliftTypeService.findById(1L);

      assertThat(result).isEqualTo(response);
    }
  }

  @Nested
  @DisplayName("Tests - findAll()")
  class FindAllTests {

    @Test
    @DisplayName("Should return paginated list of forklift types")
    void shouldReturnPaginatedList() {
      ForkliftType entity = new ForkliftType();
      ForkliftTypeResponse responseDto =
          new ForkliftTypeResponse(1L, "Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);
      PageRequest pageRequest = PageRequest.of(0, 10);
      Page<ForkliftType> page = new PageImpl<>(List.of(entity));

      when(forkliftTypeRepository.findAll(pageRequest)).thenReturn(page);
      when(forkliftTypeMapper.toResponse(entity)).thenReturn(responseDto);

      Page<ForkliftTypeResponse> result = forkliftTypeService.findAll(pageRequest);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0)).isEqualTo(responseDto);
    }
  }
}
