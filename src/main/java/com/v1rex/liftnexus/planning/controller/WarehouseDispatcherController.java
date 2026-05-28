package com.v1rex.liftnexus.planning.controller;

import com.v1rex.liftnexus.planning.dto.DispatchJobResponse;
import com.v1rex.liftnexus.planning.service.WarehouseDispatcherService;
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
public class WarehouseDispatcherController {

  private final WarehouseDispatcherService dispatcherService;

  @GetMapping("/{jobId}")
  public ResponseEntity<DispatchJobResponse> getJobStatus(@PathVariable UUID jobId) {
    log.debug("API request received to fetch status for job: {}", jobId);

    DispatchJobResponse job = dispatcherService.getJobStatusAndReconcile(jobId);

    return ResponseEntity.ok(job);
  }

  @PostMapping
  public ResponseEntity<Map<String, UUID>> submitJob() {
    log.info("API request received to trigger warehouse optimization engine.");
    UUID jobId = dispatcherService.submitOptimizationJob();

    // Returning 202 Accepted with a structured JSON body
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", jobId));
  }

  @DeleteMapping("/{jobId}")
  public ResponseEntity<Void> terminateJob(@PathVariable UUID jobId) {
    log.info("API request received to manually abort optimization job: {}", jobId);
    dispatcherService.terminateOptimizationJob(jobId);

    return ResponseEntity.noContent().build();
  }
}
