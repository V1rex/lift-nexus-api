package com.v1rex.liftnexus.planning.exception;

import java.util.UUID;

public final class DispatchJobNotFoundException extends DispatchJobDomainException {

  public DispatchJobNotFoundException(UUID jobId) {
    super(
        DispatchJobErrorCode.DISPATCH_JOB_NOT_FOUND,
        "Dispatch job with ID " + jobId + " does not exist.");
  }
}
