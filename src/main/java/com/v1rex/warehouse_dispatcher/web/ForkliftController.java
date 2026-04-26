package com.v1rex.warehouse_dispatcher.web;


import com.v1rex.warehouse_dispatcher.dto.ForkliftLocationUpdateRequest;
import com.v1rex.warehouse_dispatcher.dto.ForkliftRequest;
import com.v1rex.warehouse_dispatcher.dto.ForkliftResponse;
import com.v1rex.warehouse_dispatcher.service.ForkliftService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/forklifts")
@Validated
@RequiredArgsConstructor
public class ForkliftController {
    private final ForkliftService forkliftService;

    @PostMapping
    public ResponseEntity<ForkliftResponse> createForklift(
            @RequestBody @Valid ForkliftRequest request
            ) {
        ForkliftResponse savedForklift = forkliftService.createForklift(request);

        URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(savedForklift.id())
        .toUri();

        return ResponseEntity.created(location).body(savedForklift);
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<ForkliftResponse> updateForkliftLocation(
            @PathVariable Long id,
            @Valid @RequestBody ForkliftLocationUpdateRequest updateRequest
            ){

        ForkliftResponse updatedForklift = forkliftService.updateForkliftLocation(id,
                updateRequest.locationId());

        return ResponseEntity.ok(updatedForklift);

    }


    @GetMapping
    public ResponseEntity<Page<ForkliftResponse>> findAllForklifts(
          @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(forkliftService.findAll(pageable));
    }


    @GetMapping("/search")
    public ResponseEntity<Page<ForkliftResponse>> findWithCapacity(
            @RequestParam @Min(1) Integer minCapacity,
            @PageableDefault(size = 10, sort = "weightCapacity") Pageable pageable
    ) {
        return ResponseEntity.ok(forkliftService.findWithCapacityGreaterThan(minCapacity, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForkliftResponse> getForkliftById(@PathVariable Long id) {
        return ResponseEntity.ok(forkliftService.findById(id));
    }



}
