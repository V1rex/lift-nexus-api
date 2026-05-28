package com.v1rex.liftnexus.planning.domain;

public enum JobStatus {
  QUEUED, // Ticket created, waiting for an available solver thread
  SOLVING, // Timefold is actively calculating routes
  ABORTED, // Job terminated by the user
  COMPLETED, // Solver finished gracefully
  FAILED // An exception occurred during optimization
}
