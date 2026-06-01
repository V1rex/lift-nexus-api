package com.v1rex.liftnexus.planning.controller;

import com.v1rex.liftnexus.planning.dto.DispatchJobResponse;
import com.v1rex.liftnexus.planning.service.WarehouseDispatcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dispatcher/jobs")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Dispatcher", description = "Submit and manage Timefold optimization jobs")
public class WarehouseDispatcherController {

  private final WarehouseDispatcherService dispatcherService;

  @Operation(
      summary = "Get optimization job status",
      description =
          "Retrieves the current status and result of a previously submitted optimization job.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Job status retrieved"),
    @ApiResponse(
        responseCode = "404",
        description = "Job not found",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @GetMapping("/{jobId}")
  public ResponseEntity<DispatchJobResponse> getJobStatus(@PathVariable UUID jobId) {
    log.debug("API request received to fetch status for job: {}", jobId);

    DispatchJobResponse job = dispatcherService.getJobStatusAndReconcile(jobId);

    return ResponseEntity.ok(job);
  }

  @Operation(
      summary = "Submit a new optimization job",
      description =
          "Triggers the Timefold optimization engine to compute optimal forklift routing and task assignments asynchronously. Returns a job ID for tracking progress.")
  @ApiResponse(responseCode = "202", description = "Optimization job accepted and queued")
  @PostMapping
  public ResponseEntity<Map<String, UUID>> submitJob() {
    log.info("API request received to trigger warehouse optimization engine.");
    UUID jobId = dispatcherService.submitOptimizationJob();

    // Returning 202 Accepted with a structured JSON body
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", jobId));
  }

  @Operation(
      summary = "Terminate an optimization job",
      description =
          "Aborts a running or queued optimization job. Jobs that have already completed will remain in COMPLETED state.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Job terminated"),
    @ApiResponse(
        responseCode = "404",
        description = "Job not found",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @DeleteMapping("/{jobId}")
  public ResponseEntity<Void> terminateJob(@PathVariable UUID jobId) {
    log.info("API request received to manually abort optimization job: {}", jobId);
    dispatcherService.terminateOptimizationJob(jobId);

    return ResponseEntity.noContent().build();
  }
}
