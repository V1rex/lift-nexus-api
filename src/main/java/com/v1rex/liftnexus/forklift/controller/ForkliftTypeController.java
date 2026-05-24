package com.v1rex.liftnexus.forklift.controller;

import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import com.v1rex.liftnexus.forklift.service.ForkliftTypeService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/forklift-types")
@Validated
@RequiredArgsConstructor
public class ForkliftTypeController {

  private final ForkliftTypeService forkliftTypeService;

  @PostMapping
  public ResponseEntity<ForkliftTypeResponse> createForkliftType(
      @RequestBody @Valid ForkliftTypeRequest request) {
    ForkliftTypeResponse savedType = forkliftTypeService.createForkliftType(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedType.id())
            .toUri();

    return ResponseEntity.created(location).body(savedType);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ForkliftTypeResponse> getForkliftTypeById(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftTypeService.findById(id));
  }

  @GetMapping
  public ResponseEntity<Page<ForkliftTypeResponse>> getAllForkliftTypes(
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(forkliftTypeService.findAll(pageable));
  }
}
