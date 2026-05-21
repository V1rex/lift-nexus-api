package com.v1rex.liftnexus.config.dev;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.location.repository.LocationRepository;
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
public class DataSeeder implements CommandLineRunner {
  private final LocationRepository locationRepository;
  private final ForkliftRepository forkliftRepository;
  private final TaskRepository taskRepository;

  @Override
  @Transactional
  public void run(String... args) {
    if (locationRepository.count() > 0) {
      log.info("Warehouse already has data. Skipping seed.");
      return;
    }

    log.info("-------- Seeding Warehouse Dispatcher data ---------- ");

    Location dock = Location.builder().latitude(0.0f).longitude(0.0f).build();
    Location zoneA = Location.builder().latitude(10.0f).longitude(5.0f).build();
    Location zoneB = Location.builder().latitude(-5.0f).longitude(15.0f).build();
    Location shipping = Location.builder().latitude(20.0f).longitude(20.0f).build();
    locationRepository.saveAll(List.of(dock, zoneA, zoneB, shipping));

    Forklift heavyTruck =
        Forklift.builder()
            .weightCapacity(5000)
            .equipmentType(EquipmentType.SIDE_LOADER)
            .currentLocation(dock)
            .build();

    Forklift reachTruck =
        Forklift.builder()
            .weightCapacity(2000)
            .equipmentType(EquipmentType.REACH_TRUCK)
            .currentLocation(zoneA)
            .build();

    forkliftRepository.saveAll(List.of(heavyTruck, reachTruck));

    Task activeTask =
        Task.builder()
            .pickLocation(zoneB)
            .deliveryLocation(shipping)
            .weight(500)
            .status(TaskStatus.IN_PROGRESS)
            .requiredEquipment(EquipmentType.REACH_TRUCK)
            .forklift(reachTruck) // Assigning it manually because it's IN_PROGRESS
            .build();

    reachTruck.getTasks().add(activeTask);

    Task heavyTask =
        Task.builder()
            .pickLocation(zoneA)
            .deliveryLocation(shipping)
            .weight(4000)
            .status(TaskStatus.OPEN)
            .requiredEquipment(EquipmentType.SIDE_LOADER)
            .build();

    Task openTask =
        Task.builder()
            .pickLocation(dock)
            .deliveryLocation(zoneB)
            .weight(100)
            .status(TaskStatus.OPEN)
            .requiredEquipment(EquipmentType.STANDARD)
            .build();

    taskRepository.saveAll(List.of(activeTask, heavyTask, openTask));

    log.info(
        "Seeding complete: {} Locations, {} Forklifts, {} Tasks.",
        locationRepository.count(),
        forkliftRepository.count(),
        taskRepository.count());
  }
}
