package com.v1rex.liftnexus.storagebin.controller;

import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/storage-bins")
@Validated
@RequiredArgsConstructor
public class StorageBinController {

  private final StorageBinService storageBinService;

  @GetMapping("/{id}")
  public ResponseEntity<StorageBinResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(storageBinService.findById(id));
  }

  @GetMapping
  public ResponseEntity<Page<StorageBinResponse>> findAllStorageBins(
      @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(storageBinService.findAll(pageable));
  }

  @PostMapping
  public ResponseEntity<StorageBinResponse> createStorageBin(
      @RequestBody @Valid StorageBinRequest request) {

    StorageBinResponse savedBin = storageBinService.createStorageBin(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedBin.id())
            .toUri();

    return ResponseEntity.created(location).body(savedBin);
  }
}
