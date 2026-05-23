package com.v1rex.liftnexus.loadunit.controller;

import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
import com.v1rex.liftnexus.loadunit.service.LoadUnitService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/load-units")
@Validated
@Slf4j
@RequiredArgsConstructor
public class LoadUnitController {

  private final LoadUnitService loadUnitService;

  @PostMapping
  public ResponseEntity<LoadUnitResponse> createLoadUnit(
      @RequestBody @Valid LoadUnitRequest request) {
    log.info("REST request to create Load Unit with tracking code: {}", request.trackingCode());
    LoadUnitResponse savedUnit = loadUnitService.createLoadUnit(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedUnit.id())
            .toUri();

    return ResponseEntity.created(location).body(savedUnit);
  }

  @GetMapping("/{id}")
  public ResponseEntity<LoadUnitResponse> findById(@PathVariable Long id) {
    log.info("REST request to get Load Unit by ID: {}", id);
    return ResponseEntity.ok(loadUnitService.findById(id));
  }

  @GetMapping("/tracking/{trackingCode}")
  public ResponseEntity<LoadUnitResponse> findByTrackingCode(@PathVariable String trackingCode) {
    log.info("REST request to get Load Unit by tracking code: {}", trackingCode);
    return ResponseEntity.ok(loadUnitService.findByTrackingCode(trackingCode));
  }

  @GetMapping
  public ResponseEntity<Page<LoadUnitResponse>> findAll(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    log.info(
        "REST request to get all Load Units (Page size: {}, Page number: {})",
        pageable.getPageSize(),
        pageable.getPageNumber());
    return ResponseEntity.ok(loadUnitService.findAll(pageable));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<Page<LoadUnitResponse>> findByStatus(
      @PathVariable LoadUnitStatus status,
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    log.info("REST request to get Load Units by status: {}", status);
    return ResponseEntity.ok(loadUnitService.findByStatus(status, pageable));
  }
}
