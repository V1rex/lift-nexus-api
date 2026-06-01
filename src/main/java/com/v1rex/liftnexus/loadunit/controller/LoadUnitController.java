package com.v1rex.liftnexus.loadunit.controller;

import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
import com.v1rex.liftnexus.loadunit.service.LoadUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Load Units",
    description = "Manage load units (pallets, containers) and their tracking")
public class LoadUnitController {

  private final LoadUnitService loadUnitService;

  @Operation(
      summary = "Register a new load unit",
      description =
          "Creates a new load unit with a unique tracking code, weight, initial status, and optional storage bin assignment.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Load unit created"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = @io.swagger.v3.oas.annotations.media.Content),
    @ApiResponse(
        responseCode = "409",
        description = "Tracking code already exists",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
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

  @Operation(
      summary = "Get a load unit by ID",
      description = "Retrieves details of a specific load unit.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Load unit found"),
    @ApiResponse(
        responseCode = "404",
        description = "Load unit not found",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<LoadUnitResponse> findById(@PathVariable Long id) {
    log.info("REST request to get Load Unit by ID: {}", id);
    return ResponseEntity.ok(loadUnitService.findById(id));
  }

  @Operation(
      summary = "Find a load unit by tracking code",
      description = "Retrieves a load unit using its unique tracking code.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Load unit found"),
    @ApiResponse(
        responseCode = "404",
        description = "Load unit not found",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @GetMapping("/tracking/{trackingCode}")
  public ResponseEntity<LoadUnitResponse> findByTrackingCode(@PathVariable String trackingCode) {
    log.info("REST request to get Load Unit by tracking code: {}", trackingCode);
    return ResponseEntity.ok(loadUnitService.findByTrackingCode(trackingCode));
  }

  @Operation(
      summary = "List all load units",
      description = "Returns a paginated list of all load units in the system.")
  @ApiResponse(responseCode = "200", description = "Paginated list of load units")
  @GetMapping
  public ResponseEntity<Page<LoadUnitResponse>> findAll(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    log.info(
        "REST request to get all Load Units (Page size: {}, Page number: {})",
        pageable.getPageSize(),
        pageable.getPageNumber());
    return ResponseEntity.ok(loadUnitService.findAll(pageable));
  }

  @Operation(
      summary = "Filter load units by status",
      description =
          "Returns a paginated list of load units filtered by their current status (e.g., STORED, IN_TRANSIT).")
  @ApiResponse(responseCode = "200", description = "Matching load units")
  @GetMapping("/status/{status}")
  public ResponseEntity<Page<LoadUnitResponse>> findByStatus(
      @Parameter(description = "Load unit status to filter by") @PathVariable LoadUnitStatus status,
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    log.info("REST request to get Load Units by status: {}", status);
    return ResponseEntity.ok(loadUnitService.findByStatus(status, pageable));
  }
}
