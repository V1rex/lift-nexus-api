package com.v1rex.liftnexus.planning.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.storagebin.repository.StorageBinRepository;
import com.v1rex.liftnexus.transportorder.repository.TransportOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseDispatcherService {
  private final StorageBinRepository storageBinRepository;
  private final ForkliftRepository forkliftRepository;
  private final TransportOrderRepository transportOrderRepository;

  private final SolverManager<WarehouseSchedule> solverManager;

  private WarehouseSchedule bestSolution;
  private static final Long SINGLETON_JOB_ID = 1L;

  /*
    public WarehouseSchedule buildCurrentState() {
      log.info("Building current warehouse state for optimization...");
      List<StorageBin> storageBins = storageBinRepository.findAll();
      List<Forklift> forklifts = forkliftRepository.findAll();
      List<TransportOrder> transportOrders = transportOrderRepository.findAll();

      log.debug(
          "Found {} storageBins, {} forklifts, and {} transportOrders in DB.",
          storageBins.size(),
          forklifts.size(),
          transportOrders.size());

      WarehouseSchedule schedule = new WarehouseSchedule();
      schedule.setStorageBins(storageBins);
      schedule.setForklifts(forklifts);
      schedule.setTransportOrderPool(transportOrders);

      // 3. Return the fully loaded state ready for optimization
      return schedule;
    }

    public void startSolving() {
      MDC.put("jobId", SINGLETON_JOB_ID.toString());
      log.info("Attempting to start solver...");
      try {
        WarehouseSchedule problem = buildCurrentState();
        // 2. Start Solving
        solverManager.solveAndListen(SINGLETON_JOB_ID,
                problem, this::saveSolution);

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
  */

  /*  @Transactional
  public void saveSolution(WarehouseSchedule solution) {

    MDC.put("jobId", SINGLETON_JOB_ID.toString());

    log.info("New best solution found! Score: {}", solution.getScore());

    if (solution.getScore().isFeasible()) {
      log.info("Solution is feasible. Updating transportorder assignments in database.");
      try {
        for (Forklift forklift : solution.getForklifts()) {
          for (TransportOrder transportOrder : forklift.getTransportOrders()) {
            transportOrder.setForklift(forklift);
            transportOrderRepository.save(transportOrder);
          }
          forkliftRepository.save(forklift);
        }
        log.debug("Database sync complete for all forklifts and transportOrders.");
      } catch (Exception e) {
        log.error("Failed to persist solution to database: ", e);
      }
    } else {
      log.warn("Latest solution is infeasible (Score: {}). Skipping DB save.", solution.getScore());
    }

    this.bestSolution = solution;
    MDC.remove("jobId");
  }*/
}
