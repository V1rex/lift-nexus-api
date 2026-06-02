package com.v1rex.liftnexus.forklift.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.v1rex.liftnexus.config.TestContainersConfiguration;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
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
public class ForkliftTypeRepositoryTest {

  @Autowired private ForkliftTypeRepository forkliftTypeRepository;

  @Nested
  @DisplayName("Database Constraint & Validation Tests")
  class ConstraintTests {

    @Test
    @DisplayName("Should throw exception when model name is duplicated (Unique Constraint)")
    void shouldThrowException_WhenModelNameIsDuplicated() {
      ForkliftType type1 =
          ForkliftType.builder()
              .modelName("Model-X")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      ForkliftType type2 =
          ForkliftType.builder()
              .modelName("Model-X")
              .equipmentType(EquipmentType.REACH_TRUCK)
              .maxCapacityKg(2000)
              .totalBatteryCapacitykWh(60.0)
              .baseEnergyConsumptionPerMeter(0.6)
              .build();

      forkliftTypeRepository.saveAndFlush(type1);

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(type2))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when model name is null (@NotBlank)")
    void shouldThrowException_WhenModelNameIsNull() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName(null)
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when model name is empty string (@NotBlank)")
    void shouldThrowException_WhenModelNameIsEmpty() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when model name is only spaces (@NotBlank)")
    void shouldThrowException_WhenModelNameIsBlank() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("    ")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when equipment type is null (@NotNull)")
    void shouldThrowException_WhenEquipmentTypeIsNull() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("Model-Valid")
              .equipmentType(null)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when max capacity is zero (@Positive)")
    void shouldThrowException_WhenCapacityIsZero() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("Model-Y")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(0)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when max capacity is negative (@Positive)")
    void shouldThrowException_WhenCapacityIsNegative() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("Model-Y")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(-500)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when battery capacity is zero (@Positive)")
    void shouldThrowException_WhenBatteryCapacityIsZero() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("Model-B")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(0.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when battery capacity is negative (@Positive)")
    void shouldThrowException_WhenBatteryCapacityIsNegative() {

      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("Model-B")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(-12.5)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when base energy consumption is zero (@Positive)")
    void shouldThrowException_WhenEnergyConsumptionIsZero() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("Model-E")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.0)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when base energy consumption is negative (@Positive)")
    void shouldThrowException_WhenEnergyConsumptionIsNegative() {
      ForkliftType invalidType =
          ForkliftType.builder()
              .modelName("Model-E")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(-0.1)
              .build();

      assertThatThrownBy(() -> forkliftTypeRepository.saveAndFlush(invalidType))
          .isInstanceOf(ConstraintViolationException.class);
    }
  }

  @Nested
  @DisplayName("Custom Query Method Tests")
  class QueryTests {

    @Test
    @DisplayName("existsByModelName should return true if exists, false otherwise")
    void shouldCheckExistenceByModelName() {
      ForkliftType type =
          ForkliftType.builder()
              .modelName("Model-Z")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      forkliftTypeRepository.saveAndFlush(type);

      assertTrue(forkliftTypeRepository.existsByModelName("Model-Z"));
      assertFalse(forkliftTypeRepository.existsByModelName("Model-None"));
    }

    @Test
    @DisplayName("findByModelName should find structural data object by exact match name string")
    void shouldFindByModelName() {
      ForkliftType type =
          ForkliftType.builder()
              .modelName("Model-A")
              .equipmentType(EquipmentType.STANDARD)
              .maxCapacityKg(1000)
              .totalBatteryCapacitykWh(50.0)
              .baseEnergyConsumptionPerMeter(0.5)
              .build();

      forkliftTypeRepository.saveAndFlush(type);

      Optional<ForkliftType> found = forkliftTypeRepository.findByModelName("Model-A");

      assertTrue(found.isPresent());
      assertThat(found.get().getModelName()).isEqualTo("Model-A");
    }

    @Test
    @DisplayName(
        "findByModelName should return empty optional container if no database matches exist")
    void shouldReturnEmptyOptionalWhenModelDoesNotExist() {
      Optional<ForkliftType> found = forkliftTypeRepository.findByModelName("Model-NonExistent");
      assertFalse(found.isPresent());
    }
  }
}
