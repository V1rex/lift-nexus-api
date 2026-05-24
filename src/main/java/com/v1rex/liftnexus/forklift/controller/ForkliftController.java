package com.v1rex.liftnexus.forklift.controller;

import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftLocationUpdateRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
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
public class ForkliftController {

  private final ForkliftService forkliftService;

  @GetMapping("/{id}")
  public ResponseEntity<ForkliftResponse> getForkliftById(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftService.findById(id));
  }

  @GetMapping
  public ResponseEntity<Page<ForkliftResponse>> findAllForklifts(
      @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(forkliftService.findAll(pageable));
  }

  @GetMapping("/search")
  public ResponseEntity<Page<ForkliftResponse>> findWithCapacity(
      @RequestParam(required = false) @Min(1) Integer minCapacity,
      @RequestParam(required = false) OperationalStatus status,
      @PageableDefault(size = 10, sort = "fleetNumber") Pageable pageable) {

    if (minCapacity != null) {
      return ResponseEntity.ok(forkliftService.findWithCapacityGreaterThan(minCapacity, pageable));
    } else if (status != null) {
      return ResponseEntity.ok(forkliftService.findByStatus(status, pageable));
    }
    return ResponseEntity.ok(forkliftService.findAll(pageable));
  }

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

  @PutMapping("/{id}/location")
  public ResponseEntity<ForkliftResponse> updateForkliftLocation(
      @PathVariable Long id, @Valid @RequestBody ForkliftLocationUpdateRequest updateRequest) {
    return ResponseEntity.ok(
        forkliftService.updateForkliftLocation(id, updateRequest.locationId()));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<ForkliftResponse> updateOperationalStatus(
      @PathVariable Long id, @RequestParam OperationalStatus status) {
    return ResponseEntity.ok(forkliftService.updateOperationalStatus(id, status));
  }
}
