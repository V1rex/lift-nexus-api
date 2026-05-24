package com.v1rex.liftnexus.transportorder.controller;

import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.service.TransportOrderService;
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
public class TransportOrderController {

  private final TransportOrderService transportOrderService;

  @GetMapping("/{id}")
  public ResponseEntity<TransportOrderResponse> getOrderById(@PathVariable Long id) {
    return ResponseEntity.ok(transportOrderService.findById(id));
  }

  @GetMapping("/search")
  public ResponseEntity<Page<TransportOrderResponse>> searchOrders(
      @RequestParam(required = false) TransportOrderStatus status,
      @RequestParam(required = false) @Min(1) Integer minWeight,
      @PageableDefault(size = 20, sort = "id") Pageable pageable) {
    return ResponseEntity.ok(transportOrderService.searchOrders(status, minWeight, pageable));
  }

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

  @PutMapping("/{id}/status")
  public ResponseEntity<TransportOrderResponse> updateOrderStatus(
      @PathVariable Long id, @RequestBody @Valid TransportOrderStatusUpdateRequest request) {
    return ResponseEntity.ok(transportOrderService.updateOrderStatus(id, request));
  }
}
