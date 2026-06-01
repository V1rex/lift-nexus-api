package com.v1rex.liftnexus.forklift.controller;

import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import com.v1rex.liftnexus.forklift.service.ForkliftTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Forklift Types",
    description = "Manage forklift type catalog (models, capacity, equipment)")
public class ForkliftTypeController {

  private final ForkliftTypeService forkliftTypeService;

  @Operation(
      summary = "Create a new forklift type",
      description =
          "Registers a new forklift model/type in the catalog with its capacity and energy specifications.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Forklift type created"),
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Model name already exists",
        content = @Content)
  })
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

  @Operation(
      summary = "Get a forklift type by ID",
      description = "Retrieves details of a specific forklift type/model.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Forklift type found"),
    @ApiResponse(responseCode = "404", description = "Forklift type not found", content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<ForkliftTypeResponse> getForkliftTypeById(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftTypeService.findById(id));
  }

  @Operation(
      summary = "List all forklift types",
      description = "Returns a paginated list of all available forklift types in the catalog.")
  @ApiResponse(responseCode = "200", description = "Paginated list of forklift types")
  @GetMapping
  public ResponseEntity<Page<ForkliftTypeResponse>> getAllForkliftTypes(
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(forkliftTypeService.findAll(pageable));
  }
}
