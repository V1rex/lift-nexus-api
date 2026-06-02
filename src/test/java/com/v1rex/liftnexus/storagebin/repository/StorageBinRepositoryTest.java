package com.v1rex.liftnexus.storagebin.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.v1rex.liftnexus.config.TestContainersConfiguration;
import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("StorageBin Repository Constraints & Integrity Tests")
public class StorageBinRepositoryTest {
  @Autowired private StorageBinRepository storageBinRepository;

  @Nested
  @DisplayName("Database Level Constraints")
  class DatabaseConstraints {

    @Test
    @DisplayName("Should enforce data integrity when an explicit unique bin code is duplicated")
    void shouldThrowException_WhenBinCodeIsDuplicated() {
      StorageBin bin1 =
          StorageBin.builder()
              .binCode("DUPLICATE")
              .coordinate(new Coordinate3D(1, 1, 1))
              .zoneType(ZoneType.STORAGE)
              .maxWeightCapacityKg(1000)
              .build();

      StorageBin bin2 =
          StorageBin.builder()
              .binCode("DUPLICATE")
              .coordinate(new Coordinate3D(2, 2, 2))
              .zoneType(ZoneType.HAZMAT)
              .maxWeightCapacityKg(500)
              .build();

      storageBinRepository.save(bin1);

      assertThatThrownBy(() -> storageBinRepository.saveAndFlush(bin2))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("JPA Entity Validation Constraints")
  class ValidationConstraints {

    @Test
    @DisplayName("Should fail validation when embedded coordinate object is missing entirely")
    void shouldThrowException_WhenCoordinatesAreNull() {
      StorageBin invalidBin =
          StorageBin.builder()
              .binCode("VALID-CODE-1")
              .coordinate(null) // Violates @NotNull
              .zoneType(ZoneType.STORAGE)
              .maxWeightCapacityKg(1000)
              .build();

      assertThatThrownBy(() -> storageBinRepository.saveAndFlush(invalidBin))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("coordinate");
    }

    @Test
    @DisplayName("Should fail validation when the max weight capacity is negative")
    void shouldThrowException_WhenWeightCapacityIsNegative() {
      StorageBin invalidBin =
          StorageBin.builder()
              .binCode("VALID-CODE-3")
              .coordinate(new Coordinate3D(1, 1, 1))
              .zoneType(ZoneType.STORAGE)
              .maxWeightCapacityKg(-500) // Violates @Min(0)
              .build();

      assertThatThrownBy(() -> storageBinRepository.saveAndFlush(invalidBin))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("maxWeightCapacityKg");
    }

    @Test
    @DisplayName("Should fail validation when the bin code is omitted")
    void shouldThrowException_WhenBinCodeIsNull() {
      StorageBin invalidBin =
          StorageBin.builder()
              .binCode(null) // Violates @NotNull
              .coordinate(new Coordinate3D(1, 1, 1))
              .zoneType(ZoneType.STORAGE)
              .maxWeightCapacityKg(1000)
              .build();

      assertThatThrownBy(() -> storageBinRepository.saveAndFlush(invalidBin))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("binCode");
    }
  }
}
