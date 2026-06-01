package com.v1rex.liftnexus.planning.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Processing state of a Timefold optimization job")
public enum JobStatus {
  @Schema(description = "Job is queued and waiting for an available solver thread")
  QUEUED,
  @Schema(description = "Timefold is actively calculating optimal routes and assignments")
  SOLVING,
  @Schema(description = "Job was manually terminated by the user")
  ABORTED,
  @Schema(description = "Solver finished gracefully with a valid solution")
  COMPLETED,
  @Schema(description = "An exception occurred during optimization")
  FAILED
}
