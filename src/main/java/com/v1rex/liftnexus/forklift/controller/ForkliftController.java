package com.v1rex.liftnexus.forklift.controller;

import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftLocationUpdateRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/forklifts")
@Validated
@RequiredArgsConstructor
@Tag(name = "Forklifts", description = "Manage forklift assets and their operational status")
public class ForkliftController {

  private final ForkliftService forkliftService;

  @Operation(
      summary = "Get a forklift by ID",
      description =
          "Retrieves details of a specific forklift, including its current status, battery level, and assigned transport orders.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Forklift found"),
    @ApiResponse(responseCode = "404", description = "Forklift not found", content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<ForkliftResponse> getForkliftById(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftService.findById(id));
  }

  @Operation(
      summary = "List all forklifts",
      description = "Returns a paginated list of all forklifts in the fleet.")
  @ApiResponse(responseCode = "200", description = "Paginated list of forklifts")
  @GetMapping
  public ResponseEntity<Page<ForkliftResponse>> findAllForklifts(
      @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(forkliftService.findAll(pageable));
  }

  @Operation(
      summary = "Search forklifts by capacity or status",
      description =
          "Filters forklifts by minimum capacity or operational status. If no filter is provided, returns all forklifts.")
  @ApiResponse(responseCode = "200", description = "Matching forklifts")
  @GetMapping("/search")
  public ResponseEntity<Page<ForkliftResponse>> findWithCapacity(
      @Parameter(description = "Minimum capacity in kg") @RequestParam(required = false) @Min(1)
          Integer minCapacity,
      @Parameter(description = "Filter by operational status") @RequestParam(required = false)
          OperationalStatus status,
      @PageableDefault(size = 10, sort = "fleetNumber") Pageable pageable) {

    if (minCapacity != null) {
      return ResponseEntity.ok(forkliftService.findWithCapacityGreaterThan(minCapacity, pageable));
    } else if (status != null) {
      return ResponseEntity.ok(forkliftService.findByStatus(status, pageable));
    }
    return ResponseEntity.ok(forkliftService.findAll(pageable));
  }

  @Operation(
      summary = "Register a new forklift",
      description = "Creates a new forklift asset in the fleet.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Forklift created"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input or business rule violation",
        content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Fleet number already exists",
        content = @Content)
  })
  @PostMapping
  public ResponseEntity<ForkliftResponse> createForklift(
      @RequestBody @Valid ForkliftRequest request) {
    ForkliftResponse savedForklift = forkliftService.createForklift(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedForklift.id())
            .toUri();

    return ResponseEntity.created(location).body(savedForklift);
  }

  @Operation(
      summary = "Update forklift location",
      description = "Moves a forklift to a different storage bin location.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Location updated"),
    @ApiResponse(
        responseCode = "404",
        description = "Forklift or storage bin not found",
        content = @Content)
  })
  @PutMapping("/{id}/location")
  public ResponseEntity<ForkliftResponse> updateForkliftLocation(
      @PathVariable Long id, @Valid @RequestBody ForkliftLocationUpdateRequest updateRequest) {
    return ResponseEntity.ok(
        forkliftService.updateForkliftLocation(id, updateRequest.locationId()));
  }

  @Operation(
      summary = "Update forklift operational status",
      description =
          "Changes the operational status of a forklift (e.g., ACTIVE, MAINTENANCE, CHARGING, OFFLINE).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status updated"),
    @ApiResponse(responseCode = "404", description = "Forklift not found", content = @Content)
  })
  @PatchMapping("/{id}/status")
  public ResponseEntity<ForkliftResponse> updateOperationalStatus(
      @PathVariable Long id, @RequestParam OperationalStatus status) {
    return ResponseEntity.ok(forkliftService.updateOperationalStatus(id, status));
  }
}
