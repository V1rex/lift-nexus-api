package com.v1rex.liftnexus.storagebin.controller;

import com.v1rex.liftnexus.common.exception.ProblemDetailFactory;
import com.v1rex.liftnexus.storagebin.exception.StorageBinDomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.v1rex.liftnexus.storagebin")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class StorageBinExceptionHandler {

  private final ProblemDetailFactory errorFactory;

  @ExceptionHandler(StorageBinDomainException.class)
  public ResponseEntity<ProblemDetail> handleStorageBinDomainException(
      StorageBinDomainException ex, HttpServletRequest request) {

    log.warn(
        "Domain anomaly tracked [{}] | Context: {}", ex.getErrorCode().getCode(), ex.getMessage());

    return errorFactory.createErrorResponse(ex.getErrorCode(), ex.getMessage(), request, List.of());
  }
}
