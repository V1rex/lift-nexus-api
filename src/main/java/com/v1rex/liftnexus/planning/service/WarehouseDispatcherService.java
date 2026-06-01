package com.v1rex.liftnexus.planning.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
import com.v1rex.liftnexus.planning.domain.DispatchJob;
import com.v1rex.liftnexus.planning.domain.JobStatus;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.planning.dto.DispatchJobResponse;
import com.v1rex.liftnexus.planning.exception.DispatchJobInvalidStateException;
import com.v1rex.liftnexus.planning.exception.DispatchJobNotFoundException;
import com.v1rex.liftnexus.planning.mapper.DispatchJobMapper;
import com.v1rex.liftnexus.planning.repository.DispatchJobRepository;
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

/**
 * Service responsible for orchestrating warehouse optimisation using Timefold Solver.
 *
 * <p>Manages the full lifecycle of a dispatch-optimisation job: submitting new jobs, monitoring
 * their status, terminating running jobs, and persisting the final forklift-to-transport-order
 * assignments. Acts as the bridge between the warehouse domain (bins, forklifts, transport orders)
 * and the constraint-solving engine.
 *
 * <p><b>Job lifecycle:</b> {@code QUEUED → SOLVING → COMPLETED / FAILED / ABORTED}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseDispatcherService {

  private final StorageBinService storageBinService;
  private final ForkliftService forkliftService;
  private final TransportOrderService transportOrderService;

  private final DispatchJobRepository jobRepository;
  private final DispatchJobMapper dispatchJobMapper;

  /** Timefold solver manager that runs optimisation in a separate thread pool. */
  private final SolverManager<WarehouseSchedule> solverManager;

  /**
   * Returns the current status of a dispatch job.
   *
   * <p>Intended to be extended with reconciliation logic that detects when Timefold has silently
   * stopped solving (e.g., due to an internal error) while the job is still marked as {@code
   * SOLVING} in the database, and marks it as {@code FAILED} accordingly.
   *
   * @param jobId The unique identifier of the dispatch job.
   * @return A response DTO with the current job state.
   * @throws DispatchJobNotFoundException if no job exists for the given ID.
   */
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

  /**
   * Assembles the current warehouse snapshot that the solver will optimise.
   *
   * <p>Loads all storage bins, forklifts, and pending transport orders from the database into an
   * in-memory {@link WarehouseSchedule} object. This serves as the problem fact set for the
   * Timefold constraint solver.
   *
   * <p><b>Known issue:</b> Loading <i>all</i> entities without pagination is a performance
   * bottleneck for large datasets. A more selective fetching strategy (cursor-based pagination,
   * lazy fields, caching) should be implemented.
   *
   * @return A fully populated {@link WarehouseSchedule} representing the current state of the
   *     warehouse.
   */
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

  /**
   * Submits a new asynchronous optimisation job to Timefold.
   *
   * <p>Creates a {@link DispatchJob} record in state {@code QUEUED}, then delegates to {@link
   * SolverManager#solveAndListen} with:
   *
   * <ol>
   *   <li>A problem-fetcher that transitions the job to {@code SOLVING} and builds the current
   *       warehouse state.
   *   <li>A best-solution consumer that persists the final assignments and marks the job as {@code
   *       COMPLETED} (or {@code FAILED}).
   * </ol>
   *
   * @return The UUID assigned to the newly created optimisation job. The caller can use this ID to
   *     poll status ({@link #getJobStatusAndReconcile}) or to terminate the job ({@link
   *     #terminateOptimizationJob}).
   */
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

  /**
   * Attempts to gracefully terminate a running or queued optimisation job.
   *
   * <p>Only jobs in state {@code QUEUED} or {@code SOLVING} can be terminated. Once terminated, the
   * job is marked as {@code ABORTED} and any partial results are discarded (the best-solution
   * consumer checks for this flag).
   *
   * @param jobId The unique identifier of the job to terminate.
   * @throws DispatchJobInvalidStateException if the job is already in a terminal state ({@code
   *     COMPLETED}, {@code FAILED}, or {@code ABORTED}).
   */
  @Transactional
  public void terminateOptimizationJob(UUID jobId) {
    log.info("Request received to manually terminate optimization Job: {}", jobId);

    DispatchJob job = findJobEntityById(jobId);

    if (job.getStatus() != JobStatus.QUEUED && job.getStatus() != JobStatus.SOLVING) {
      throw new DispatchJobInvalidStateException(
          "Cannot terminate job " + jobId + " because it is already in status: " + job.getStatus());
    }

    // Ask Timefold to stop solving as soon as possible
    solverManager.terminateEarly(jobId);

    job.setStatus(JobStatus.ABORTED);
    job.setCompletedAt(Instant.now());
    jobRepository.save(job);

    log.info("Job {} has been successfully halted and marked as ABORTED.", jobId);
  }

  /**
   * Fetcher callback used by Timefold at the start of solving.
   *
   * <p>Transitions the job from {@code QUEUED} to {@code SOLVING} in the database and then builds
   * the current warehouse snapshot. If building the snapshot fails, the job is marked as {@code
   * FAILED} and the exception is rethrown to Timefold.
   *
   * <p><b>Note:</b> This method is annotated {@code @Transactional} so that the status update and
   * snapshot build happen within the same persistence context.
   *
   * @param jobId The unique identifier of the job being started.
   * @return A fully populated {@link WarehouseSchedule} for the solver.
   */
  @Transactional
  public WarehouseSchedule buildCurrentProblemAndSetSolvingStatus(UUID jobId) {
    log.info("Worker thread starting optimization for Job: {}", jobId);

    DispatchJob job = findJobEntityById(jobId);

    job.setStatus(JobStatus.SOLVING);
    jobRepository.save(job);

    try {
      return buildCurrentState();
    } catch (Exception e) {
      log.error("Failed to build current state for job {}: {}", jobId, e.getMessage(), e);
      job.setStatus(JobStatus.FAILED);
      job.setCompletedAt(Instant.now());
      jobRepository.save(job);
      throw e;
    }
  }

  /**
   * Best-solution consumer callback invoked by Timefold when a solution is available (either the
   * final optimal solution or an intermediate best-effort one).
   *
   * <p>Persists the solver's assignments back to the database:
   *
   * <ul>
   *   <li>Updates forklift assignments on transport orders.
   *   <li>Records assigned orders on forklifts.
   * </ul>
   *
   * <p>If the job was {@code ABORTED} during solving, the solution is silently discarded. Otherwise
   * the job is marked as {@code COMPLETED} (or {@code FAILED} if persistence fails). Even
   * infeasible solutions are saved as a best-effort fallback.
   *
   * @param solution The {@link WarehouseSchedule} produced by Timefold, which contains the
   *     optimised assignments.
   * @param jobId The unique identifier of the job that produced this solution.
   */
  @Transactional
  public void saveFinalSolution(WarehouseSchedule solution, UUID jobId) {
    log.info("Optimization completed for Job: {}", jobId);

    DispatchJob job = findJobEntityById(jobId);

    // Discard the result if the job was externally aborted while solving
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

    try {
      // Persist the optimised assignments to the underlying domain entities
      transportOrderService.updateForkliftAssignments(solution.getTransportOrderPool());
      forkliftService.updateAssignedOrders(solution.getForklifts());

      job.setStatus(JobStatus.COMPLETED);
      if (solution.getScore() != null) {
        job.setFinalScore(solution.getScore().toString());
      }
    } catch (Exception e) {
      log.error("Failed to persist solution for job {}: {}", jobId, e.getMessage(), e);
      job.setStatus(JobStatus.FAILED);
    } finally {
      job.setCompletedAt(Instant.now());
      jobRepository.save(job);
    }

    log.info("Job {} successfully wrapped and saved.", jobId);
  }

  /**
   * Retrieves the raw {@link DispatchJob} entity by its ID.
   *
   * @param jobId The unique identifier of the dispatch job.
   * @return The managed {@link DispatchJob} entity.
   * @throws DispatchJobNotFoundException if no job exists for the given ID.
   */
  public DispatchJob findJobEntityById(UUID jobId) {
    return jobRepository.findById(jobId).orElseThrow(() -> new DispatchJobNotFoundException(jobId));
  }
}
