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
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ForkliftTravelDistanceConstraintTest {
  private ConstraintVerifier<ForkliftTravelDistanceTestConstraintProvider, WarehouseSchedule>
      constraintVerifier;

  private ForkliftType mockForkliftType;
  private Forklift mockForklift;

  @BeforeEach
  void setUp() {
    constraintVerifier =
        ConstraintVerifier.build(
            new ForkliftTravelDistanceTestConstraintProvider(),
            WarehouseSchedule.class,
            TransportOrder.class,
            Forklift.class);

    StorageBin dock =
        StorageBin.builder()
            .id(1L)
            .binCode("DOCK-01")
            .coordinate(new Coordinate3D(0, 0, 0))
            .zoneType(ZoneType.STAGING_OUT)
            .maxWeightCapacityKg(10_000)
            .build();

    mockForkliftType =
        ForkliftType.builder()
            .id(1L)
            .modelName("MOCK-Forklift-Type")
            .equipmentType(EquipmentType.STANDARD)
            .maxCapacityKg(1_000)
            .totalBatteryCapacitykWh(50.0)
            .baseEnergyConsumptionPerMeter(0.25)
            .build();

    mockForklift =
        Forklift.builder()
            .id(1L)
            .fleetNumber("FLEET-001")
            .forkliftType(mockForkliftType)
            .currentStorageBin(dock)
            .currentBatteryPercentage(100.0)
            .transportOrders(new ArrayList<TransportOrder>())
            .build();
  }

  @Test
  @DisplayName("Forklift travel distance should not penalize when no transport orders are assigned")
  void forkliftTravelDistance_shouldNotPenalize_WhenEmptyTransportOrders() {
    constraintVerifier
        .verifyThat(ForkliftTravelDistanceTestConstraintProvider::forkliftTravelDistance)
        .given(mockForklift)
        .penalizesBy(0);
  }

  @Test
  @DisplayName("Forklift travel distance should penalize when one transport order is assigned")
  void forkliftTravelDistance_shouldPenalize_WhenOneTransportOrder() {

    LoadUnit mockLoad =
        LoadUnit.builder()
            .id(1L)
            .trackingCode("MOCK-LOAD-001")
            .weightKg(500)
            .status(LoadUnitStatus.STAGED)
            .currentBin(mockForklift.getCurrentStorageBin())
            .build();

    StorageBin targetDock =
        StorageBin.builder()
            .id(1L)
            .binCode("DOCK-01")
            .coordinate(new Coordinate3D(10, 0, 0))
            .zoneType(ZoneType.STAGING_OUT)
            .maxWeightCapacityKg(10_000)
            .build();

    TransportOrder order =
        TransportOrder.builder()
            .id(1L)
            .sourceBin(mockForklift.getCurrentStorageBin())
            // we use the current Storage Bin with the following coordinates
            // (0,0,0)
            .targetBin(targetDock)
            .targetLoadUnit(mockLoad)
            .assignedForklift(mockForklift)
            .build();

    mockForklift.getTransportOrders().add(order);

    constraintVerifier
        .verifyThat(ForkliftTravelDistanceTestConstraintProvider::forkliftTravelDistance)
        .given(mockForklift)
        .penalizesBy(10); // the travel distance should be penalized by 10
  }

  @Test
  @DisplayName(
      "Forklift travel distance should penalize sequentially when more than one transport order is assigned")
  void forkliftTravelDistance_shouldPenalize_WhenMoreThanOneTransportOrder() {

    LoadUnit mockLoad1 =
        LoadUnit.builder()
            .id(1L)
            .trackingCode("MOCK-LOAD-001")
            .weightKg(500)
            .status(LoadUnitStatus.STAGED)
            .currentBin(mockForklift.getCurrentStorageBin()) // (0,0,0)
            .build();

    StorageBin targetDock1 =
        StorageBin.builder()
            .id(2L)
            .binCode("DOCK-02")
            .coordinate(new Coordinate3D(10, 0, 0))
            .zoneType(ZoneType.STAGING_OUT)
            .maxWeightCapacityKg(10_000)
            .build();

    TransportOrder order1 =
        TransportOrder.builder()
            .id(1L)
            .sourceBin(mockForklift.getCurrentStorageBin())
            .targetBin(targetDock1)
            .targetLoadUnit(mockLoad1)
            .assignedForklift(mockForklift)
            .build();

    StorageBin sourceBin2 =
        StorageBin.builder()
            .id(3L)
            .binCode("AISLE-A-01")
            .coordinate(new Coordinate3D(15, 0, 0))
            .zoneType(ZoneType.STORAGE)
            .maxWeightCapacityKg(5_000)
            .build();

    StorageBin targetBin2 =
        StorageBin.builder()
            .id(4L)
            .binCode("AISLE-A-12")
            .coordinate(new Coordinate3D(25, 0, 0))
            .zoneType(ZoneType.STORAGE)
            .maxWeightCapacityKg(5_000)
            .build();

    LoadUnit mockLoad2 =
        LoadUnit.builder()
            .id(2L)
            .trackingCode("MOCK-LOAD-002")
            .weightKg(300)
            .status(LoadUnitStatus.STAGED)
            .currentBin(sourceBin2)
            .build();

    TransportOrder order2 =
        TransportOrder.builder()
            .id(2L)
            .sourceBin(sourceBin2)
            .targetBin(targetBin2)
            .targetLoadUnit(mockLoad2)
            .assignedForklift(mockForklift)
            .build();

    mockForklift.getTransportOrders().add(order1);
    mockForklift.getTransportOrders().add(order2);

    // Total calculated distance verification:
    // Order 1 loaded trip: |0 - 10| = 10
    // Deadhead trip to Order 2: |10 - 15| = 5
    // Order 2 loaded trip: |15 - 25| = 10
    // Total = 25
    constraintVerifier
        .verifyThat(ForkliftTravelDistanceTestConstraintProvider::forkliftTravelDistance)
        .given(mockForklift)
        .penalizesBy(25);
  }

  private static final class ForkliftTravelDistanceTestConstraintProvider
      implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
      return new Constraint[] {
        ForkliftTravelDistanceConstraint.forkliftTravelDistance(constraintFactory)
      };
    }

    public Constraint forkliftTravelDistance(ConstraintFactory factory) {
      return ForkliftTravelDistanceConstraint.forkliftTravelDistance(factory);
    }
  }
}
