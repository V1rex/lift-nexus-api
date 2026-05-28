package com.v1rex.liftnexus.planning.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
import com.v1rex.liftnexus.planning.DispatchJobRepository;
import com.v1rex.liftnexus.planning.domain.DispatchJob;
import com.v1rex.liftnexus.planning.domain.JobStatus;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.planning.dto.DispatchJobResponse;
import com.v1rex.liftnexus.planning.mapper.DispatchJobMapper;
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
  private final DispatchJobMapper dispatchJobMapper;

  private final SolverManager<WarehouseSchedule> solverManager;

  public DispatchJobResponse getJobStatusAndReconcile(UUID jobId) {
    DispatchJob job = findJobEntityById(jobId);
    // TODO: look for a better way to update the job status in case of TimeFold failure
    /*
     SolverStatus timefoldStatus = solverManager.getSolverStatus(jobId);
     if (job.getStatus() == JobStatus.SOLVING && timefoldStatus == SolverStatus.NOT_SOLVING) {
        log.error("Reconciliation Alert: Job {} is {} in DB, " +
                        "but Timefold is NOT_SOLVING. Marking job as FAILED.",
        jobId, job.getStatus());

        job.setStatus(JobStatus.FAILED);
        job.setCompletedAt(Instant.now());
        jobRepository.save(job);

    }*/

    return dispatchJobMapper.toResponse(job);
  }

  public WarehouseSchedule buildCurrentState() {
    log.info("Building current warehouse state for optimization...");

    // TODO: this is a really great performance bottleneck.
    //  If the database contains thousands of storage bins,
    //  forklifts, and transport orders, this method will take a long
    //  time to execute and may cause the worker thread to time out before optimization can even
    // begin.
    //  Action: Create in Milestone 2 an issue to fix the fetching of storageBins, forklifts, and
    // transportOrders
    //  by implementing a more efficient data retrieval strategy (e.g., pagination, selective field
    // fetching, or caching).
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
  public void terminateOptimizationJob(UUID jobId) {
    log.info("Request received to manually terminate optimization Job: {}", jobId);

    DispatchJob job = findJobEntityById(jobId);

    if (job.getStatus() != JobStatus.QUEUED && job.getStatus() != JobStatus.SOLVING) {
      throw new IllegalStateException(
          "Cannot terminate job " + jobId + " because it is already in status: " + job.getStatus());
    }

    solverManager.terminateEarly(jobId);

    job.setStatus(JobStatus.ABORTED);
    job.setCompletedAt(Instant.now());
    jobRepository.save(job);

    log.info("Job {} has been successfully halted and marked as ABORTED.", jobId);
  }

  @Transactional
  public WarehouseSchedule buildCurrentProblemAndSetSolvingStatus(UUID jobId) {
    log.info("Worker thread starting optimization for Job: {}", jobId);

    DispatchJob job = findJobEntityById(jobId);

    job.setStatus(JobStatus.SOLVING);
    jobRepository.save(job);

    return buildCurrentState();
  }

  @Transactional
  public void saveFinalSolution(WarehouseSchedule solution, UUID jobId) {
    log.info("Optimization completed for Job: {}", jobId);

    DispatchJob job = findJobEntityById(jobId);

    if (job.getStatus() == JobStatus.ABORTED) {
      log.warn("Job {} was aborted during optimization. Final solution will not be saved.", jobId);
      return;
    }

    if (solution.getScore() != null && solution.getScore().isFeasible()) {
      log.info("Solution is feasible (Score: {}). Saving assignments to DB.", solution.getScore());
    } else {
      log.warn(
          "Solution is INFEASIBLE (Score: {}). Saving best-effort assignments anyway.",
          solution.getScore());
    }

    // TODO: Wrap data updates in a try-catch block. If transportOrderService or forkliftService
    //       throws an exception here, the job status will remain stuck in 'SOLVING'.
    //       Catch exceptions and mark the job status as JobStatus.FAILED.

    transportOrderService.updateForkliftAssignments(solution.getTransportOrderPool());
    forkliftService.updateAssignedOrders(solution.getForklifts());

    job.setStatus(JobStatus.COMPLETED);
    job.setCompletedAt(Instant.now());

    if (solution.getScore() != null) {
      job.setFinalScore(solution.getScore().toString());
    }
    jobRepository.save(job);
    log.info("Job {} successfully wrapped and saved.", jobId);
  }

  public DispatchJob findJobEntityById(UUID jobId) {
    return jobRepository
        .findById(jobId)
        // TODO: implement proper Custom Exception in the API
        .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
  }
}
