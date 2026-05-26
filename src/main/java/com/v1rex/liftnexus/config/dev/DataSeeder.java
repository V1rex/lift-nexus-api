package com.v1rex.liftnexus.config.dev;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
import com.v1rex.liftnexus.forklift.repository.ForkliftTypeRepository;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.repository.LoadUnitRepository;
import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import com.v1rex.liftnexus.storagebin.repository.StorageBinRepository;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.repository.TransportOrderRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
  private final StorageBinRepository storageBinRepository;
  private final ForkliftRepository forkliftRepository;
  private final ForkliftTypeRepository forkliftTypeRepository;
  private final TransportOrderRepository transportOrderRepository;
  private final LoadUnitRepository loadUnitRepository;

  @Override
  @Transactional
  public void run(String... args) {
    if (storageBinRepository.count() > 0) {
      log.info("Warehouse already has data. Skipping seed.");
      return;
    }

    log.info("-------- Seeding Warehouse Dispatcher data---------- ");

    ForkliftType sideLoaderType =
        ForkliftType.builder()
            .modelName("SIDEL-2000")
            .equipmentType(EquipmentType.SIDE_LOADER)
            .maxCapacityKg(5000)
            .totalBatteryCapacitykWh(150.0)
            .baseEnergyConsumptionPerMeter(0.5)
            .build();

    ForkliftType reachType =
        ForkliftType.builder()
            .modelName("REACH-1000")
            .equipmentType(EquipmentType.REACH_TRUCK)
            .maxCapacityKg(2000)
            .totalBatteryCapacitykWh(80.0)
            .baseEnergyConsumptionPerMeter(0.3)
            .build();

    forkliftTypeRepository.saveAll(List.of(sideLoaderType, reachType));

    StorageBin dock =
        StorageBin.builder()
            .binCode("DOCK-01")
            .coordinate(new Coordinate3D(0, 0, 0))
            .zoneType(ZoneType.STAGING_OUT)
            .maxWeightCapacityKg(10000)
            .build();

    StorageBin zoneA =
        StorageBin.builder()
            .binCode("A-01")
            .coordinate(new Coordinate3D(10, 5, 0))
            .zoneType(ZoneType.STORAGE)
            .maxWeightCapacityKg(2000)
            .build();

    StorageBin zoneB =
        StorageBin.builder()
            .binCode("B-01")
            .coordinate(new Coordinate3D(-5, 15, 0))
            .zoneType(ZoneType.STORAGE)
            .maxWeightCapacityKg(2000)
            .build();

    StorageBin shipping =
        StorageBin.builder()
            .binCode("SHIP-01")
            .coordinate(new Coordinate3D(20, 20, 0))
            .zoneType(ZoneType.STAGING_IN)
            .maxWeightCapacityKg(5000)
            .build();

    storageBinRepository.saveAll(List.of(dock, zoneA, zoneB, shipping));

    LoadUnit lu1 =
        LoadUnit.builder()
            .trackingCode("LU-1000")
            .weightKg(500)
            .status(LoadUnitStatus.STAGED)
            .currentBin(zoneB)
            .build();
    LoadUnit lu2 =
        LoadUnit.builder()
            .trackingCode("LU-2000")
            .weightKg(4000)
            .status(LoadUnitStatus.STAGED)
            .currentBin(zoneA)
            .build();
    LoadUnit lu3 =
        LoadUnit.builder()
            .trackingCode("LU-3000")
            .weightKg(100)
            .status(LoadUnitStatus.STAGED)
            .currentBin(dock)
            .build();

    loadUnitRepository.saveAll(List.of(lu1, lu2, lu3));

    Forklift heavyTruck =
        Forklift.builder()
            .fleetNumber("FLEET-HEAVY-01")
            .forkliftType(sideLoaderType)
            .currentStorageBin(dock)
            .status(OperationalStatus.ACTIVE)
            .currentBatteryPercentage(85.0)
            .transportOrders(new ArrayList<>())
            .build();

    Forklift reachTruck =
        Forklift.builder()
            .fleetNumber("FLEET-REACH-01")
            .forkliftType(reachType)
            .currentStorageBin(zoneA)
            .status(OperationalStatus.ACTIVE)
            .currentBatteryPercentage(60.0)
            .transportOrders(new ArrayList<>())
            .build();

    forkliftRepository.saveAll(List.of(heavyTruck, reachTruck));

    TransportOrder activeOrder =
        TransportOrder.builder()
            .sourceBin(zoneB)
            .targetBin(shipping)
            .targetLoadUnit(lu1)
            .status(TransportOrderStatus.IN_PROGRESS)
            .requiredEquipment(EquipmentType.REACH_TRUCK)
            .assignedForklift(reachTruck)
            .build();

    reachTruck.getTransportOrders().add(activeOrder);

    TransportOrder heavyOrder =
        TransportOrder.builder()
            .sourceBin(zoneA)
            .targetBin(shipping)
            .targetLoadUnit(lu2)
            .status(TransportOrderStatus.OPEN)
            .requiredEquipment(EquipmentType.SIDE_LOADER)
            .build();

    TransportOrder openOrder =
        TransportOrder.builder()
            .sourceBin(dock)
            .targetBin(zoneB)
            .targetLoadUnit(lu3)
            .status(TransportOrderStatus.OPEN)
            .requiredEquipment(EquipmentType.STANDARD)
            .build();

    transportOrderRepository.saveAll(List.of(activeOrder, heavyOrder, openOrder));

    log.info(
        "Seeding complete: {} Locations, {} ForkliftTypes, {} Forklifts, {} LoadUnits, {} Orders.",
        storageBinRepository.count(),
        forkliftTypeRepository.count(),
        forkliftRepository.count(),
        loadUnitRepository.count(),
        transportOrderRepository.count());
  }
}
