package com.v1rex.liftnexus.planning.constraints;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TransportOrderEquipmentRequirementConstraintTest {

  private ConstraintVerifier<EquipmentRequirementTestConstraintProvider, WarehouseSchedule>
      constraintVerifier;
  private StorageBin mockBin;

  @BeforeEach
  void setUp() {
    constraintVerifier =
        ConstraintVerifier.build(
            new EquipmentRequirementTestConstraintProvider(),
            WarehouseSchedule.class,
            TransportOrder.class,
            Forklift.class);

    mockBin =
        StorageBin.builder()
            .id(1L)
            .binCode("BIN-01")
            .coordinate(new Coordinate3D(0, 0, 0))
            .zoneType(ZoneType.STORAGE)
            .maxWeightCapacityKg(5_000)
            .build();
  }

  @Test
  @DisplayName(
      "Equipment requirement should NOT penalize when forklift type matches required equipment")
  void equipmentType_shouldNotPenalize_WhenEquipmentMatches() {
    ForkliftType clampType =
        ForkliftType.builder()
            .id(1L)
            .modelName("Standard-Forklift-Typ")
            .equipmentType(EquipmentType.STANDARD)
            .build();

    Forklift forklift =
        Forklift.builder().id(1L).fleetNumber("Forklift-01").forkliftType(clampType).build();

    TransportOrder order =
        TransportOrder.builder()
            .id(1L)
            .sourceBin(mockBin)
            .targetBin(mockBin)
            .requiredEquipment(EquipmentType.STANDARD) // Matches forklift!
            .assignedForklift(forklift)
            .build();

    constraintVerifier
        .verifyThat(EquipmentRequirementTestConstraintProvider::equipmentType)
        .given(order)
        .penalizesBy(0);
  }

  @Test
  @DisplayName(
      "Equipment requirement should penalize with 1 HARD when forklift type mis-matches required equipment")
  void equipmentType_shouldPenalize_WhenEquipmentMismatches() {
    ForkliftType standardType =
        ForkliftType.builder()
            .id(2L)
            .modelName("Standard-Forklift")
            .equipmentType(EquipmentType.STANDARD)
            .build();

    Forklift forklift =
        Forklift.builder()
            .id(2L)
            .fleetNumber("FLEET-STANDARD-01")
            .forkliftType(standardType)
            .build();

    TransportOrder order =
        TransportOrder.builder()
            .id(2L)
            .sourceBin(mockBin)
            .targetBin(mockBin)
            .requiredEquipment(EquipmentType.REACH_TRUCK)
            .assignedForklift(forklift)
            .build();

    constraintVerifier
        .verifyThat(EquipmentRequirementTestConstraintProvider::equipmentType)
        .given(order)
        .penalizesBy(1);
  }

  @Test
  @DisplayName(
      "Equipment requirement should NOT penalize when order has no specific equipment requirements")
  void equipmentType_shouldNotPenalize_WhenNoEquipmentRequired() {
    ForkliftType standardType =
        ForkliftType.builder().id(2L).equipmentType(EquipmentType.STANDARD).build();

    Forklift forklift = Forklift.builder().id(2L).forkliftType(standardType).build();

    TransportOrder order =
        TransportOrder.builder()
            .id(3L)
            .sourceBin(mockBin)
            .targetBin(mockBin)
            .requiredEquipment(null)
            .assignedForklift(forklift)
            .build();

    constraintVerifier
        .verifyThat(EquipmentRequirementTestConstraintProvider::equipmentType)
        .given(order)
        .penalizesBy(0);
  }

  private static final class EquipmentRequirementTestConstraintProvider
      implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
      return new Constraint[] {
        TransportOrderEquipmentRequirementConstraint.equipmentType(constraintFactory)
      };
    }

    public Constraint equipmentType(ConstraintFactory factory) {
      return TransportOrderEquipmentRequirementConstraint.equipmentType(factory);
    }
  }
}
