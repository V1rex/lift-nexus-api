package com.v1rex.liftnexus.forklift.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.v1rex.liftnexus.config.TestContainersConfiguration;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
public class ForkliftRepositoryTest {

  @Autowired private ForkliftRepository forkliftRepository;

  @Autowired private ForkliftTypeRepository forkliftTypeRepository;

  private ForkliftType lightType;
  private ForkliftType heavyType;

  @BeforeEach
  void setUp() {
    lightType =
        forkliftTypeRepository.save(
            ForkliftType.builder()
                .modelName("Light Model")
                .equipmentType(EquipmentType.PALLET_JACK)
                .maxCapacityKg(1000)
                .totalBatteryCapacitykWh(50.0)
                .baseEnergyConsumptionPerMeter(0.5)
                .build());

    heavyType =
        forkliftTypeRepository.save(
            ForkliftType.builder()
                .modelName("Heavy Model")
                .equipmentType(EquipmentType.REACH_TRUCK)
                .maxCapacityKg(3000)
                .totalBatteryCapacitykWh(150.0)
                .baseEnergyConsumptionPerMeter(2.0)
                .build());
  }

  @Nested
  @DisplayName("Database Constraint & Validation Tests")
  class ConstraintTests {

    @Test
    @DisplayName("Should throw exception when fleet number is null (@NotBlank)")
    void shouldThrowException_WhenFleetNumberIsNull() {
      Forklift invalidForklift =
          Forklift.builder().fleetNumber(null).forkliftType(lightType).build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when fleet number is empty string (@NotBlank)")
    void shouldThrowException_WhenFleetNumberIsEmpty() {
      Forklift invalidForklift = Forklift.builder().fleetNumber("").forkliftType(lightType).build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when fleet number is blank string (@NotBlank)")
    void shouldThrowException_WhenFleetNumberIsBlank() {
      Forklift invalidForklift =
          Forklift.builder().fleetNumber("    ").forkliftType(lightType).build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when forklift type reference is null (@NotNull)")
    void shouldThrowException_WhenForkliftTypeIsNull() {
      Forklift invalidForklift =
          Forklift.builder().fleetNumber("FL-VALID-ID").forkliftType(null).build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when fleet number is duplicated (Unique Constraint)")
    void shouldThrowException_WhenFleetNumberIsDuplicated() {
      Forklift forklift1 = Forklift.builder().fleetNumber("FL-01").forkliftType(lightType).build();
      Forklift forklift2 = Forklift.builder().fleetNumber("FL-01").forkliftType(heavyType).build();

      forkliftRepository.saveAndFlush(forklift1);

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(forklift2))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should apply default builder values for status and battery percentage")
    void shouldApplyDefaultValues() {
      Forklift forklift =
          Forklift.builder().fleetNumber("FL-DEFAULTS").forkliftType(lightType).build();

      Forklift saved = forkliftRepository.saveAndFlush(forklift);

      assertThat(saved.getStatus()).isEqualTo(OperationalStatus.OFFLINE);
      assertThat(saved.getCurrentBatteryPercentage()).isEqualTo(100.0);
      assertThat(saved.getTransportOrders()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when operational status is explicitly set to null")
    void shouldThrowException_WhenStatusIsNull() {
      Forklift invalidForklift =
          Forklift.builder()
              .fleetNumber("FL-NULL-STATUS")
              .forkliftType(lightType)
              .status(null)
              .build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should throw exception when current battery percentage is explicitly set to null")
    void shouldThrowException_WhenBatteryPercentageIsNull() {
      Forklift invalidForklift =
          Forklift.builder()
              .fleetNumber("FL-NULL-BATTERY")
              .forkliftType(lightType)
              .currentBatteryPercentage(null)
              .build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("Custom Query Method Tests")
  class QueryTests {

    @BeforeEach
    void seedData() {
      Forklift activeForklift =
          Forklift.builder()
              .fleetNumber("FL-01")
              .forkliftType(lightType)
              .status(OperationalStatus.ACTIVE)
              .build();

      Forklift maintenanceForklift =
          Forklift.builder()
              .fleetNumber("FL-02")
              .forkliftType(heavyType)
              .status(OperationalStatus.MAINTENANCE)
              .build();

      forkliftRepository.saveAll(List.of(activeForklift, maintenanceForklift));
      forkliftRepository.flush();
    }

    @Test
    @DisplayName("existsByFleetNumber should return true if exists, false otherwise")
    void shouldCheckExistenceByFleetNumber() {
      assertTrue(forkliftRepository.existsByFleetNumber("FL-01"));
      assertFalse(forkliftRepository.existsByFleetNumber("FL-NON-EXISTENT"));
    }

    @Test
    @DisplayName("findById should fetch forklift with initialized EntityGraph targets")
    void shouldFindByIdWithEntityGraph() {
      Forklift target = forkliftRepository.findAll().get(0);

      Optional<Forklift> resultOpt = forkliftRepository.findById(target.getId());

      assertTrue(resultOpt.isPresent());
      Forklift result = resultOpt.get();
      assertEquals(target.getFleetNumber(), result.getFleetNumber());
      assertNotNull(result.getForkliftType());
      assertEquals(
          target.getForkliftType().getModelName(), result.getForkliftType().getModelName());
    }

    @Test
    @DisplayName("findAll() without arguments should utilize EntityGraph successfully")
    void shouldExecuteFindAllListWithEntityGraph() {
      List<Forklift> results = forkliftRepository.findAll();

      assertThat(results.size()).isEqualTo(2);
      assertThat(results.get(0).getForkliftType()).isNotNull();
    }

    @Test
    @DisplayName("findAll(Pageable) should return a paginated slice utilizing EntityGraph elements")
    void shouldExecuteFindAllPagedWithEntityGraph() {
      Page<Forklift> pageResult = forkliftRepository.findAll(PageRequest.of(0, 1));

      assertNotNull(pageResult);
      assertEquals(2, pageResult.getTotalElements());
      assertEquals(1, pageResult.getContent().size());
      assertNotNull(pageResult.getContent().get(0).getForkliftType());
    }

    @Test
    @DisplayName("Should correctly filter and paginate forklifts by Operational Status")
    void shouldFindForkliftsByStatus() {
      Page<Forklift> result =
          forkliftRepository.findByStatus(OperationalStatus.ACTIVE, PageRequest.of(0, 10));

      assertEquals(1, result.getTotalElements());
      assertEquals("FL-01", result.getContent().get(0).getFleetNumber());
    }

    @Test
    @DisplayName("Should return empty page configuration if no forklift matches status filter")
    void shouldReturnEmptyPageWhenNoStatusMatches() {
      Page<Forklift> result =
          forkliftRepository.findByStatus(OperationalStatus.OFFLINE, PageRequest.of(0, 10));
      assertEquals(0, result.getTotalElements());
      assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName(
        "Should correctly filter and paginate forklifts by joined ForkliftType max capacity")
    void shouldFindForkliftsWithCapacityGreaterThanEqual() {
      Page<Forklift> result =
          forkliftRepository.findByForkliftType_MaxCapacityKgGreaterThanEqual(
              2000, PageRequest.of(0, 10));

      assertNotNull(result);
      assertEquals(
          1, result.getTotalElements(), "Should match exactly 1 forklift with archetype >= 2000kg");

      Forklift found = result.getContent().get(0);
      assertEquals("FL-02", found.getFleetNumber());
      assertEquals(3000, found.getForkliftType().getMaxCapacityKg());
    }
  }
}
