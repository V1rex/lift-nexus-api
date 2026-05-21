package com.v1rex.liftnexus.planning.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.location.repository.LocationRepository;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.repository.TaskRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseDispatcherService {
  private final LocationRepository locationRepository;
  private final ForkliftRepository forkliftRepository;
  private final TaskRepository taskRepository;

  private final SolverManager<WarehouseSchedule> solverManager;

  private WarehouseSchedule bestSolution;
  private static final Long SINGLETON_JOB_ID = 1L;

  public WarehouseSchedule buildCurrentState() {
    log.info("Building current warehouse state for optimization...");
    List<Location> locations = locationRepository.findAll();
    List<Forklift> forklifts = forkliftRepository.findAll();
    List<Task> tasks = taskRepository.findAll();

    log.debug(
        "Found {} locations, {} forklifts, and {} tasks in DB.",
        locations.size(),
        forklifts.size(),
        tasks.size());

    WarehouseSchedule schedule = new WarehouseSchedule();
    schedule.setLocations(locations);
    schedule.setForklifts(forklifts);
    schedule.setTaskPool(tasks);

    // 3. Return the fully loaded state ready for optimization
    return schedule;
  }

  public void startSolving() {
    MDC.put("jobId", SINGLETON_JOB_ID.toString());
    log.info("Attempting to start solver...");
    try {
      WarehouseSchedule problem = buildCurrentState();
      // 2. Start Solving
      solverManager.solveAndListen(SINGLETON_JOB_ID, problem, this::saveSolution);

      log.info("Solver successfully started in background thread.");

    } catch (Exception e) {
      log.error("Critical failure while starting the solver: ", e);
    } finally {
      // Clear MDC so other logs don't get 'polluted' with this JobId
      MDC.remove("jobId");
    }
  }

  public WarehouseSchedule getSolution() {
    return bestSolution != null ? bestSolution : buildCurrentState();
  }

  @Transactional
  public void saveSolution(WarehouseSchedule solution) {

    MDC.put("jobId", SINGLETON_JOB_ID.toString());

    log.info("New best solution found! Score: {}", solution.getScore());

    if (solution.getScore().isFeasible()) {
      log.info("Solution is feasible. Updating task assignments in database.");
      try {
        for (Forklift forklift : solution.getForklifts()) {
          for (Task task : forklift.getTasks()) {
            task.setForklift(forklift);
            taskRepository.save(task);
          }
          forkliftRepository.save(forklift);
        }
        log.debug("Database sync complete for all forklifts and tasks.");
      } catch (Exception e) {
        log.error("Failed to persist solution to database: ", e);
      }
    } else {
      log.warn("Latest solution is infeasible (Score: {}). Skipping DB save.", solution.getScore());
    }

    this.bestSolution = solution;
    MDC.remove("jobId");
  }
}
