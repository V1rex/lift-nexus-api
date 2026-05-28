package com.v1rex.liftnexus.planning.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
import com.v1rex.liftnexus.planning.DispatchJobRepository;
import com.v1rex.liftnexus.planning.domain.DispatchJob;
import com.v1rex.liftnexus.planning.domain.JobStatus;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.service.TransportOrderService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseDispatcherService {
  private final StorageBinService storageBinService;
  private final ForkliftService forkliftService;
  private final TransportOrderService transportOrderService;

  private final DispatchJobRepository jobRepository;

  private final SolverManager<WarehouseSchedule> solverManager;

  public WarehouseSchedule buildCurrentState() {
    log.info("Building current warehouse state for optimization...");
    List<StorageBin> storageBins = storageBinService.findAllEntities();
    List<Forklift> forklifts = forkliftService.findAllEntities();
    List<TransportOrder> transportOrders = transportOrderService.findAllEntities();

    log.debug(
        "Found {} storageBins, {} forklifts, and {} transportOrders in DB.",
        storageBins.size(),
        forklifts.size(),
        transportOrders.size());

    WarehouseSchedule schedule = new WarehouseSchedule();
    schedule.setStorageBins(storageBins);
    schedule.setForklifts(forklifts);
    schedule.setTransportOrderPool(transportOrders);

    return schedule;
  }

  public UUID submitOptimizationJob() {
    UUID ticketId = UUID.randomUUID();
    DispatchJob job =
        DispatchJob.builder()
            .id(ticketId)
            .status(JobStatus.QUEUED)
            .createdAt(Instant.now())
            .build();
    jobRepository.save(job);

    solverManager.solveAndListen(
        ticketId,
        buildCurrentProblemAndSetSolvingStatus(ticketId),
        solution -> saveFinalSolution(solution, ticketId));

    return ticketId;
  }

  @Transactional
  public WarehouseSchedule buildCurrentProblemAndSetSolvingStatus(UUID jobId) {
    log.info("Worker thread starting optimization for Job: {}", jobId);

    DispatchJob job =
        jobRepository
            .findById(jobId)
            // TODO: implement proper Custom Exception in the API
            .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
    job.setStatus(JobStatus.SOLVING);
    jobRepository.save(job);

    return buildCurrentState();
  }

  @Transactional
  public void saveFinalSolution(WarehouseSchedule solution, UUID jobId) {
    log.info("Optimization completed for Job: {}", jobId);
    // TODO: implement solution persistence logic here (e.g. save to DB, publish events, etc.)

  }
}
