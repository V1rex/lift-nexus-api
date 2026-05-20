package com.v1rex.liftnexus.location.controller;


import com.v1rex.liftnexus.location.dto.LocationRequest;
import com.v1rex.liftnexus.location.dto.LocationResponse;
import com.v1rex.liftnexus.location.service.LocationService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/locations")
@Validated
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(
            @RequestBody @Valid LocationRequest request
            ){

        LocationResponse savedLocation = locationService.createLocation(request);


        URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(savedLocation.id())
        .toUri();

        return ResponseEntity.created(location).body(savedLocation);
    }

    @GetMapping
    public ResponseEntity<Page<LocationResponse>> findAllLocations(
            @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ){
        return ResponseEntity.ok(locationService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> findById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(locationService.findById(id));
    }
}
