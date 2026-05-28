package com.v1rex.liftnexus.planning.dto;

import com.v1rex.liftnexus.planning.domain.JobStatus;
import java.time.Instant;
import java.util.UUID;

public record DispatchJobResponse(
    UUID id, JobStatus status, Instant createdAt, Instant completedAt, String finalScore) {}
