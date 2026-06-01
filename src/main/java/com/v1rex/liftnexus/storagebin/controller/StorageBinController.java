package com.v1rex.liftnexus.storagebin.controller;

import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Storage Bins", description = "Manage warehouse storage bin locations and coordinates")
public class StorageBinController {

  private final StorageBinService storageBinService;

  @Operation(
      summary = "Get a storage bin by ID",
      description = "Retrieves a storage bin with its 3D coordinate and zone type.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Storage bin found"),
    @ApiResponse(
        responseCode = "404",
        description = "Storage bin not found",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<StorageBinResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(storageBinService.findById(id));
  }

  @Operation(
      summary = "List all storage bins",
      description = "Returns a paginated list of all storage bins in the warehouse.")
  @ApiResponse(responseCode = "200", description = "Paginated list of storage bins")
  @GetMapping
  public ResponseEntity<Page<StorageBinResponse>> findAllStorageBins(
      @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(storageBinService.findAll(pageable));
  }

  @Operation(
      summary = "Create a new storage bin",
      description =
          "Registers a new storage bin at a specific 3D coordinate with a zone type and weight capacity.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Storage bin created"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input or coordinate already in use",
        content = @io.swagger.v3.oas.annotations.media.Content),
    @ApiResponse(
        responseCode = "409",
        description = "Bin code already exists",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
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
