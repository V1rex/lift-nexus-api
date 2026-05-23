/*
package com.v1rex.liftnexus.config.dev;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.repository.StorageBinRepository;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import com.v1rex.liftnexus.task.repository.TaskRepository;
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
public class DataSeeder
        implements CommandLineRunner {
  private final StorageBinRepository storageBinRepository;
  private final ForkliftRepository forkliftRepository;
  private final TaskRepository taskRepository;

  @Override
  @Transactional
  public void run(String... args) {
    if (storageBinRepository.count() > 0) {
      log.info("Warehouse already has data. Skipping seed.");
      return;
    }

    log.info("-------- Seeding Warehouse Dispatcher data ---------- ");

    StorageBin dock = StorageBin.builder().latitude(0.0f).longitude(0.0f).build();
    StorageBin zoneA = StorageBin.builder().latitude(10.0f).longitude(5.0f).build();
    StorageBin zoneB = StorageBin.builder().latitude(-5.0f).longitude(15.0f).build();
    StorageBin shipping = StorageBin.builder().latitude(20.0f).longitude(20.0f).build();
    storageBinRepository.saveAll(List.of(dock, zoneA, zoneB, shipping));

    Forklift heavyTruck =
        Forklift.builder()
            .weightCapacity(5000)
            .equipmentType(EquipmentType.SIDE_LOADER)
            .currentStorageBin(dock)
            .build();

    Forklift reachTruck =
        Forklift.builder()
            .weightCapacity(2000)
            .equipmentType(EquipmentType.REACH_TRUCK)
            .currentStorageBin(zoneA)
            .build();

    forkliftRepository.saveAll(List.of(heavyTruck, reachTruck));

    Task activeTask =
        Task.builder()
            .pickStorageBin(zoneB)
            .deliveryStorageBin(shipping)
            .weight(500)
            .status(TaskStatus.IN_PROGRESS)
            .requiredEquipment(EquipmentType.REACH_TRUCK)
            .forklift(reachTruck) // Assigning it manually because it's IN_PROGRESS
            .build();

    reachTruck.getTasks().add(activeTask);

    Task heavyTask =
        Task.builder()
            .pickStorageBin(zoneA)
            .deliveryStorageBin(shipping)
            .weight(4000)
            .status(TaskStatus.OPEN)
            .requiredEquipment(EquipmentType.SIDE_LOADER)
            .build();

    Task openTask =
        Task.builder()
            .pickStorageBin(dock)
            .deliveryStorageBin(zoneB)
            .weight(100)
            .status(TaskStatus.OPEN)
            .requiredEquipment(EquipmentType.STANDARD)
            .build();

    taskRepository.saveAll(List.of(activeTask, heavyTask, openTask));

    log.info(
        "Seeding complete: {} Locations, {} Forklifts, {} Tasks.",
        storageBinRepository.count(),
        forkliftRepository.count(),
        taskRepository.count());
  }
}
*/
