package com.v1rex.liftnexus.transportorder.service;

import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.service.LoadUnitService;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.exception.TransportOrderInvalidStateException;
import com.v1rex.liftnexus.transportorder.exception.TransportOrderNotFoundException;
import com.v1rex.liftnexus.transportorder.mapper.TransportOrderMapper;
import com.v1rex.liftnexus.transportorder.repository.TransportOrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer responsible for managing transport orders within the warehouse system.
 *
 * <p>Handles the full lifecycle of a transport order: creation, status transitions, assignment to
 * forklifts, and retrieval. All public methods enforce business rules such as ensuring the load
 * unit is physically present at the source bin before a transport order can be created.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransportOrderService {

  private final TransportOrderRepository transportOrderRepository;
  private final TransportOrderMapper transportOrderMapper;

  private final StorageBinService storageBinService;
  private final LoadUnitService loadUnitService;

  /**
   * Creates a new transport order that moves a load unit from a source storage bin to a destination
   * storage bin.
   *
   * <p>Validates that the specified load unit is currently located in the requested source bin
   * before proceeding. Throws an exception if the load unit is not present at the source location.
   *
   * @param request DTO containing the target load unit ID, source bin ID, and destination bin ID.
   * @return The persisted transport order wrapped in a response DTO.
   * @throws TransportOrderInvalidStateException if the load unit is not in the specified source
   *     bin.
   */
  @Transactional
  public TransportOrderResponse createTransportOrder(TransportOrderRequest request) {
    log.info(
        "Creating TransportOrder for LoadUnit: {} from Bin: {} to Bin: {}",
        request.targetLoadUnitId(),
        request.sourceBinId(),
        request.destinationBinId());

    // Resolve referenced entities
    LoadUnit loadUnit = loadUnitService.findEntityById(request.targetLoadUnitId());
    StorageBin sourceBin = storageBinService.findEntityById(request.sourceBinId());
    StorageBin destinationBin = storageBinService.findEntityById(request.destinationBinId());

    // Business rule: the load unit must physically reside in the source bin
    if (loadUnit.getCurrentBin() == null
        || !loadUnit.getCurrentBin().getId().equals(sourceBin.getId())) {
      throw new TransportOrderInvalidStateException(
          "LoadUnit "
              + loadUnit.getTrackingCode()
              + " is not located in the requested source bin.");
    }

    // Map request to entity and wire up the resolved domain objects
    TransportOrder order = transportOrderMapper.toEntity(request);
    order.setTargetLoadUnit(loadUnit);
    order.setSourceBin(sourceBin);
    order.setTargetBin(destinationBin);

    TransportOrder savedOrder = transportOrderRepository.save(order);

    log.info("Successfully created TransportOrder ID: {}", savedOrder.getId());
    return transportOrderMapper.toResponse(savedOrder);
  }

  /**
   * Advances (or changes) the status of an existing transport order.
   *
   * <p>Applies state-machine rules defined in {@link #checkStatusBeforeUpdate} to prevent invalid
   * transitions such as rolling back from {@code IN_PROGRESS} to {@code OPEN} or mutating a
   * completed order.
   *
   * @param id The unique identifier of the transport order to update.
   * @param request DTO carrying the desired new status.
   * @return The updated transport order wrapped in a response DTO.
   * @throws TransportOrderNotFoundException if no order exists for the given ID.
   * @throws TransportOrderInvalidStateException if the requested transition is not allowed.
   */
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

  /**
   * Looks up a transport order by its ID and returns the response DTO.
   *
   * @param id The unique identifier of the transport order.
   * @return The transport order response DTO.
   * @throws TransportOrderNotFoundException if no order exists for the given ID.
   */
  @Transactional(readOnly = true)
  public TransportOrderResponse findById(Long id) {
    return transportOrderMapper.toResponse(findEntityById(id));
  }

  /**
   * Searches for transport orders with optional filters and pagination.
   *
   * @param status Optional status filter (may be null).
   * @param minWeight Optional minimum weight filter (may be null).
   * @param pageable Pagination and sorting information.
   * @return A page of matching transport order response DTOs.
   */
  @Transactional(readOnly = true)
  public Page<TransportOrderResponse> searchOrders(
      TransportOrderStatus status, Integer minWeight, Pageable pageable) {
    return transportOrderRepository
        .searchOrders(status, minWeight, pageable)
        .map(transportOrderMapper::toResponse);
  }

  /**
   * Retrieves the raw {@link TransportOrder} entity by its ID.
   *
   * <p>This is used internally by other service methods that need to work with the managed JPA
   * entity rather than the response DTO.
   *
   * @param id The unique identifier of the transport order.
   * @return The managed {@link TransportOrder} entity.
   * @throws TransportOrderNotFoundException if no order exists for the given ID.
   */
  @Transactional(readOnly = true)
  public TransportOrder findEntityById(Long id) {
    return transportOrderRepository
        .findById(id)
        .orElseThrow(() -> new TransportOrderNotFoundException(id));
  }

  /**
   * Returns <strong>all</strong> transport order entities without pagination.
   *
   * <p><b>Caution:</b> This method should only be used in batch/background operations where
   * fetching the full dataset is acceptable (e.g., scheduled forklift-assignment jobs). Prefer the
   * paginated {@link #searchOrders} method for user-facing features.
   *
   * @return A list of every {@link TransportOrder} in the database.
   */
  public List<TransportOrder> findAllEntities() {
    log.info("Fetching all managed transport order entities without pagination");
    return transportOrderRepository.findAll();
  }

  /**
   * Assigns forklifts to a list of transport orders and transitions them to the {@link
   * TransportOrderStatus#ASSIGNED} state.
   *
   * <p>Each order in the provided list is fetched from the database to obtain the managed entity,
   * then updated with the assigned forklift reference. The status is automatically advanced to
   * {@code ASSIGNED}.
   *
   * @param orders List of transport order entities carrying at least the ID and the desired
   *     forklift assignment.
   */
  @Transactional
  public void updateForkliftAssignments(List<TransportOrder> orders) {
    for (TransportOrder order : orders) {
      // Re-fetch to work with the managed entity within this persistence context
      TransportOrder databaseOrder = findEntityById(order.getId());

      // Update the forklift reference
      databaseOrder.setAssignedForklift(order.getAssignedForklift());

      // Automatically transition the order to ASSIGNED status
      updateOrderStatus(
          databaseOrder.getId(),
          new TransportOrderStatusUpdateRequest(TransportOrderStatus.ASSIGNED));
    }
  }

  /**
   * Enforces the transport-order state machine rules to prevent invalid status transitions.
   *
   * <p>Currently enforced rules:
   *
   * <ul>
   *   <li>A {@code COMPLETED} order can never be changed.
   *   <li>An {@code IN_PROGRESS} order cannot be rolled back to {@code OPEN}.
   *   <li>An {@code ASSIGNED} order cannot be rolled back to {@code OPEN}.
   * </ul>
   *
   * @param id The transport order ID (used only in error messages).
   * @param currentStatus The current status of the order.
   * @param newStatus The desired new status.
   * @throws TransportOrderInvalidStateException if the transition is forbidden.
   */
  private void checkStatusBeforeUpdate(
      Long id, TransportOrderStatus currentStatus, TransportOrderStatus newStatus) {
    if (currentStatus == TransportOrderStatus.COMPLETED) {
      throw new TransportOrderInvalidStateException(
          "Cannot update TransportOrder " + id + " because it is already COMPLETED.");
    }
    if (currentStatus == TransportOrderStatus.IN_PROGRESS
        && newStatus == TransportOrderStatus.OPEN) {
      throw new TransportOrderInvalidStateException(
          "Cannot roll back TransportOrder " + id + " from ACTIVE to OPEN.");
    }
    if (currentStatus == TransportOrderStatus.ASSIGNED && newStatus == TransportOrderStatus.OPEN) {
      throw new TransportOrderInvalidStateException(
          "Cannot roll back TransportOrder " + id + " from ASSIGNED to OPEN.");
    }
  }
}
