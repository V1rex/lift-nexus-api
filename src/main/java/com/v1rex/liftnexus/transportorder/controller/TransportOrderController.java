package com.v1rex.liftnexus.transportorder.controller;

import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.service.TransportOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/v1/transport-orders")
@Validated
@RequiredArgsConstructor
@Tag(
    name = "Transport Orders",
    description = "Manage transport orders for moving loads across the warehouse")
public class TransportOrderController {

  private final TransportOrderService transportOrderService;

  @Operation(
      summary = "Get a transport order by ID",
      description =
          "Retrieves details of a specific transport order, including its source/destination bins and assigned forklift.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transport order found"),
    @ApiResponse(
        responseCode = "404",
        description = "Transport order not found",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<TransportOrderResponse> getOrderById(@PathVariable Long id) {
    return ResponseEntity.ok(transportOrderService.findById(id));
  }

  @Operation(
      summary = "Search transport orders",
      description =
          "Search transport orders by status and/or minimum weight. Returns a paginated result set.")
  @ApiResponse(responseCode = "200", description = "Matching transport orders")
  @GetMapping("/search")
  public ResponseEntity<Page<TransportOrderResponse>> searchOrders(
      @Parameter(description = "Filter by order status") @RequestParam(required = false)
          TransportOrderStatus status,
      @Parameter(description = "Minimum weight in kg") @RequestParam(required = false) @Min(1)
          Integer minWeight,
      @PageableDefault(size = 20, sort = "id") Pageable pageable) {
    return ResponseEntity.ok(transportOrderService.searchOrders(status, minWeight, pageable));
  }

  @Operation(
      summary = "Create a new transport order",
      description =
          "Creates a transport order to move a load unit from a source bin to a destination bin.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Transport order created"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @PostMapping
  public ResponseEntity<TransportOrderResponse> createOrder(
      @RequestBody @Valid TransportOrderRequest request) {
    TransportOrderResponse savedOrder = transportOrderService.createTransportOrder(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedOrder.id())
            .toUri();

    return ResponseEntity.created(location).body(savedOrder);
  }

  @Operation(
      summary = "Update transport order status",
      description =
          "Updates the status of a transport order along the lifecycle: OPEN → ASSIGNED → IN_PROGRESS → COMPLETED/FAILED.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status updated"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid state transition",
        content = @io.swagger.v3.oas.annotations.media.Content),
    @ApiResponse(
        responseCode = "404",
        description = "Transport order not found",
        content = @io.swagger.v3.oas.annotations.media.Content)
  })
  @PutMapping("/{id}/status")
  public ResponseEntity<TransportOrderResponse> updateOrderStatus(
      @PathVariable Long id, @RequestBody @Valid TransportOrderStatusUpdateRequest request) {
    return ResponseEntity.ok(transportOrderService.updateOrderStatus(id, request));
  }
}
