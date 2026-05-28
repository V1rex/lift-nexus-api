package com.v1rex.liftnexus.planning.mapper;

import com.v1rex.liftnexus.planning.domain.DispatchJob;
import com.v1rex.liftnexus.planning.dto.DispatchJobResponse;
import org.springframework.stereotype.Component;

@Component
public class DispatchJobMapper {

  public DispatchJobResponse toResponse(DispatchJob job) {
    if (job == null) {
      return null;
    }
    return new DispatchJobResponse(
        job.getId(),
        job.getStatus(),
        job.getCreatedAt(),
        job.getCompletedAt(),
        job.getFinalScore());
  }
}
