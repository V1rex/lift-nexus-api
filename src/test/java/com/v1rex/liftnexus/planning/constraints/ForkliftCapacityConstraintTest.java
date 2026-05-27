package com.v1rex.liftnexus.planning.constraints;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ForkliftCapacityConstraintTest {

  private ConstraintVerifier<ForkliftCapacityTestConstraintProvider, WarehouseSchedule>
      constraintVerifier;

  private StorageBin loadingDock;
  private ForkliftType smallType;
  private ForkliftType bigType;

  @BeforeEach
  void setUp() {
    this.constraintVerifier =
        ConstraintVerifier.build(
            new ForkliftCapacityTestConstraintProvider(),
            WarehouseSchedule.class,
            TransportOrder.class,
            Forklift.class);

    loadingDock =
        StorageBin.builder()
            .id(1L)
            .binCode("DOCK-01")
            .coordinate(new Coordinate3D(0, 0, 0))
            .zoneType(ZoneType.STAGING_OUT)
            .maxWeightCapacityKg(10_000)
            .build();

    smallType =
        ForkliftType.builder()
            .id(1L)
            .modelName("SMALL-01")
            .equipmentType(EquipmentType.STANDARD)
            .maxCapacityKg(1_000)
            .totalBatteryCapacitykWh(50.0)
            .baseEnergyConsumptionPerMeter(0.25)
            .build();

    bigType =
        ForkliftType.builder()
            .id(2L)
            .modelName("BIG-01")
            .equipmentType(EquipmentType.STANDARD)
            .maxCapacityKg(3_000)
            .totalBatteryCapacitykWh(50.0)
            .baseEnergyConsumptionPerMeter(0.25)
            .build();
  }

  @Test
  @DisplayName(
      "Forklift capacity should penalize when assigned load exceeds forklift's max capacity")
  void forkliftCapacity_shouldPenalize_whenOverloaded() {

    Forklift smallForklift =
        Forklift.builder()
            .id(1L)
            .fleetNumber("FLEET-001")
            .forkliftType(smallType)
            .currentStorageBin(loadingDock)
            .currentBatteryPercentage(100.0)
            .build();

    LoadUnit heavyLoad =
        LoadUnit.builder()
            .id(1L)
            .trackingCode("LU-HEAVY-01")
            .weightKg(2_000)
            .status(LoadUnitStatus.STAGED)
            .currentBin(loadingDock)
            .build();

    TransportOrder order =
        TransportOrder.builder()
            .id(1L)
            .sourceBin(loadingDock)
            .targetBin(loadingDock)
            .targetLoadUnit(heavyLoad)
            .assignedForklift(smallForklift)
            .build();

    constraintVerifier
        .verifyThat(ForkliftCapacityTestConstraintProvider::forkliftCapacity)
        .given(order)
        .penalizesBy(1);
  }

  @Test
  @DisplayName(
      "Forklift capacity should not penalize when assigned load is within forklift's max capacity")
  void forkliftCapacity_shouldNotPenalize_whenNotOverloaded() {
    Forklift bigForklift =
        Forklift.builder()
            .id(1L)
            .fleetNumber("FLEET-001")
            .forkliftType(bigType)
            .currentStorageBin(loadingDock)
            .currentBatteryPercentage(100.0)
            .build();

    LoadUnit heavyLoad =
        LoadUnit.builder()
            .id(1L)
            .trackingCode("LU-HEAVY-01")
            .weightKg(2_000)
            .status(LoadUnitStatus.STAGED)
            .currentBin(loadingDock)
            .build();

    TransportOrder order =
        TransportOrder.builder()
            .id(1L)
            .sourceBin(loadingDock)
            .targetBin(loadingDock)
            .targetLoadUnit(heavyLoad)
            .assignedForklift(bigForklift)
            .build();

    constraintVerifier
        .verifyThat(ForkliftCapacityTestConstraintProvider::forkliftCapacity)
        .given(order)
        .penalizesBy(0);
  }

  @Test
  @DisplayName("Forklift capacity should not penalize when the task is not assigned")
  void forkliftCapacity_shouldNotPenalize_whenNotAssigned() {
    Forklift bigForklift =
        Forklift.builder()
            .id(1L)
            .fleetNumber("FLEET-001")
            .forkliftType(bigType)
            .currentStorageBin(loadingDock)
            .currentBatteryPercentage(100.0)
            .build();

    LoadUnit heavyLoad =
        LoadUnit.builder()
            .id(1L)
            .trackingCode("LU-HEAVY-01")
            .weightKg(2_000)
            .status(LoadUnitStatus.STAGED)
            .currentBin(loadingDock)
            .build();

    TransportOrder order =
        TransportOrder.builder()
            .id(1L)
            .sourceBin(loadingDock)
            .targetBin(loadingDock)
            .targetLoadUnit(heavyLoad)
            // .assignedForklift(bigForklift) - we do not assign the order to a forklift
            .build();

    constraintVerifier
        .verifyThat(ForkliftCapacityTestConstraintProvider::forkliftCapacity)
        .given(order)
        .penalizesBy(0);
  }

  private static final class ForkliftCapacityTestConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
      return new Constraint[] {forkliftCapacity(constraintFactory)};
    }

    public Constraint forkliftCapacity(ConstraintFactory factory) {
      return ForkliftCapacityConstraint.forkliftCapacity(factory);
    }
  }
}
