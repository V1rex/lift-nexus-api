package com.v1rex.liftnexus.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailFactory {

  public ResponseEntity<ProblemDetail> createErrorResponse(
      ErrorCode errorCode, String detail, HttpServletRequest request, List<String> errors) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
    problemDetail.setTitle(errorCode.getDefaultTitle());
    problemDetail.setType(URI.create("urn:liftnexus:problem:" + errorCode.getCode()));
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", errorCode.getCode());
    problemDetail.setProperty("timestamp", Instant.now().toString());

    if (errors != null && !errors.isEmpty()) {
      problemDetail.setProperty("errors", errors);
    }

    return ResponseEntity.status(errorCode.getStatus()).body(problemDetail);
  }
}
