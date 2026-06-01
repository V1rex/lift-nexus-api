package com.v1rex.liftnexus.loadunit.controller;

import com.v1rex.liftnexus.common.exception.ProblemDetailFactory;
import com.v1rex.liftnexus.loadunit.exception.LoadUnitDomainException;
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

@RestControllerAdvice(basePackages = "com.v1rex.liftnexus.loadunit")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class LoadUnitExceptionHandler {

  private final ProblemDetailFactory errorFactory;

  @ExceptionHandler(LoadUnitDomainException.class)
  public ResponseEntity<ProblemDetail> handleLoadUnitDomainException(
      LoadUnitDomainException ex, HttpServletRequest request) {

    log.warn(
        "Domain anomaly tracked [{}] | Context: {}", ex.getErrorCode().getCode(), ex.getMessage());

    return errorFactory.createErrorResponse(ex.getErrorCode(), ex.getMessage(), request, List.of());
  }
}
