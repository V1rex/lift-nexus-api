package com.v1rex.liftnexus.transportorder.service;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.service.LoadUnitService;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.mapper.TransportOrderMapper;
import com.v1rex.liftnexus.transportorder.repository.TransportOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransportOrderService {

  private final TransportOrderRepository transportOrderRepository;
  private final TransportOrderMapper transportOrderMapper;

  private final StorageBinService storageBinService;
  private final LoadUnitService loadUnitService;

  @Transactional
  public TransportOrderResponse createTransportOrder(TransportOrderRequest request) {
    log.info(
        "Creating TransportOrder for LoadUnit: {} from Bin: {} to Bin: {}",
        request.targetLoadUnitId(),
        request.sourceBinId(),
        request.destinationBinId());

    LoadUnit loadUnit = loadUnitService.findEntityById(request.targetLoadUnitId());
    StorageBin sourceBin = storageBinService.findEntityById(request.sourceBinId());
    StorageBin destinationBin = storageBinService.findEntityById(request.destinationBinId());

    if (loadUnit.getCurrentBin() == null
        || !loadUnit.getCurrentBin().getId().equals(sourceBin.getId())) {
      throw new IllegalStateException(
          "LoadUnit "
              + loadUnit.getTrackingCode()
              + " is not located in the requested source bin.");
    }

    TransportOrder order = transportOrderMapper.toEntity(request);
    order.setTargetLoadUnit(loadUnit);
    order.setSourceBin(sourceBin);
    order.setTargetBin(destinationBin);

    TransportOrder savedOrder = transportOrderRepository.save(order);

    log.info("Successfully created TransportOrder ID: {}", savedOrder.getId());
    return transportOrderMapper.toResponse(savedOrder);
  }

  @Transactional
  public TransportOrderResponse updateOrderStatus(
      Long id, TransportOrderStatusUpdateRequest request) {
    TransportOrder order = findEntityById(id);
    TransportOrderStatus currentStatus = order.getStatus();
    TransportOrderStatus newStatus = request.status();

    checkStatusBeforeUpdate(id, currentStatus, newStatus);

    order.setStatus(newStatus);

    log.info("TransportOrder {} transitioned: {} -> {}", id, currentStatus, newStatus);
    return transportOrderMapper.toResponse(order);
  }

  @Transactional(readOnly = true)
  public TransportOrderResponse findById(Long id) {
    return transportOrderMapper.toResponse(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public Page<TransportOrderResponse> searchOrders(
      TransportOrderStatus status, Integer minWeight, Pageable pageable) {
    return transportOrderRepository
        .searchOrders(status, minWeight, pageable)
        .map(transportOrderMapper::toResponse);
  }

  public TransportOrder findEntityById(Long id) {
    return transportOrderRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("TransportOrder with ID " + id + " not found."));
  }

  private void checkStatusBeforeUpdate(
      Long id, TransportOrderStatus currentStatus, TransportOrderStatus newStatus) {
    if (currentStatus == TransportOrderStatus.COMPLETED) {
      throw new IllegalStateException(
          "Cannot update TransportOrder " + id + " because it is already COMPLETED.");
    }
    if (currentStatus == TransportOrderStatus.IN_PROGRESS
        && newStatus == TransportOrderStatus.OPEN) {
      throw new IllegalStateException(
          "Cannot roll back TransportOrder " + id + " from ACTIVE to OPEN.");
    }
    if (currentStatus == TransportOrderStatus.ASSIGNED && newStatus == TransportOrderStatus.OPEN) {
      throw new IllegalStateException(
          "Cannot roll back TransportOrder " + id + " from ASSIGNED to OPEN.");
    }
  }
}
