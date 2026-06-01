package com.v1rex.liftnexus.planning.dto;

import com.v1rex.liftnexus.planning.domain.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "View of a Timefold optimization job with its current state and results")
public record DispatchJobResponse(
    @Schema(description = "Unique job identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID id,
    @Schema(description = "Current processing status of the optimization job") JobStatus status,
    @Schema(description = "Timestamp when the job was created") Instant createdAt,
    @Schema(description = "Timestamp when the job completed or failed (nullable)", nullable = true)
        Instant completedAt,
    @Schema(
            description = "Final score from the Timefold solver (nullable before completion)",
            example = "0hard/0soft",
            nullable = true)
        String finalScore) {}
