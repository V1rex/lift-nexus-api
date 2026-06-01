package com.v1rex.liftnexus.forklift.service;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.forklift.exception.ForkliftFleetNumberExistsException;
import com.v1rex.liftnexus.forklift.exception.ForkliftNotFoundException;
import com.v1rex.liftnexus.forklift.mapper.ForkliftMapper;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for the Forklift bounded context.
 *
 * <p>Manages the full lifecycle of warehouse forklifts: registration, location tracking,
 * operational status updates, and assignment of transport orders during solver solution
 * persistence. Cross-domain communication follows the anti-corruption rule — all external entity
 * lookups go through the respective service interfaces, never directly through repositories.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ForkliftService {

  private final ForkliftRepository forkliftRepository;
  private final ForkliftMapper forkliftMapper;
  private final ForkliftTypeService forkliftTypeService;
  private final StorageBinService storageBinService;

  /**
   * Registers a new forklift in the warehouse fleet.
   *
   * <p>Validates that the fleet number is unique, resolves the forklift type (archetype) and
   * optional initial storage bin from their respective domains, persists the aggregate, and returns
   * a {@link ForkliftResponse} DTO.
   *
   * @param request the inbound payload containing fleet number, type ID, and optional initial
   *     storage bin ID (must not be {@code null}, must pass validation)
   * @return a DTO representing the newly created forklift
   * @throws ForkliftFleetNumberExistsException if a forklift with the given fleet number already
   *     exists in the system
   */
  @Transactional
  public ForkliftResponse createForklift(ForkliftRequest request) {
    log.info("Provisioning new warehouse asset with fleet number: {}", request.fleetNumber());

    if (forkliftRepository.existsByFleetNumber(request.fleetNumber())) {
      throw new ForkliftFleetNumberExistsException(request.fleetNumber());
    }

    ForkliftType forkliftType = forkliftTypeService.findEntityById(request.forkliftTypeId());

    StorageBin initialBin =
        request.currentStorageBinId() != null
            ? storageBinService.findEntityById(request.currentStorageBinId())
            : null;

    Forklift forklift = forkliftMapper.toEntity(request);
    forklift.setForkliftType(forkliftType);
    forklift.setCurrentStorageBin(initialBin);

    Forklift savedForklift = forkliftRepository.save(forklift);
    log.debug("Successfully registered asset ID {}", savedForklift.getId());
    return forkliftMapper.toResponse(savedForklift);
  }

  /**
   * Retrieves a single forklift by its unique identifier.
   *
   * @param id the forklift's database identifier (must be positive and exist)
   * @return a DTO representing the forklift
   * @throws ForkliftNotFoundException if no forklift with that ID is found
   */
  @Transactional(readOnly = true)
  public ForkliftResponse findById(Long id) {
    return forkliftMapper.toResponse(findEntityById(id));
  }

  /**
   * Returns a paginated list of all forklifts in the system.
   *
   * @param pageable pagination and sorting parameters (default sort: {@code id} ascending)
   * @return a page of forklift DTOs
   */
  @Transactional(readOnly = true)
  public Page<ForkliftResponse> findAll(Pageable pageable) {
    return findAllEntities(pageable).map(forkliftMapper::toResponse);
  }

  /**
   * Searches for forklifts by minimum lifting capacity or operational status.
   *
   * <p>If {@code minCapacity} is provided, results are filtered by the forklift type's max
   * capacity.
   *
   * @param minCapacity the minimum weight capacity in kilograms (optional, {@code >= 1})
   * @param pageable pagination parameters (default size: 10, sort: fleetNumber)
   * @return a page of matching forklift DTOs
   */
  @Transactional(readOnly = true)
  public Page<ForkliftResponse> findWithCapacityGreaterThan(
      Integer minCapacity, Pageable pageable) {
    log.info("Searching assets matching minimum operational lifting capacity: {}kg", minCapacity);
    return forkliftRepository
        .findByForkliftType_MaxCapacityKgGreaterThanEqual(minCapacity, pageable)
        .map(forkliftMapper::toResponse);
  }

  /**
   * Searches for forklifts by operational status.
   *
   * @param status the operational status to filter by (e.g. {@code ACTIVE}, {@code OFFLINE})
   * @param pageable pagination parameters
   * @return a page of forklift DTOs matching the given status
   */
  @Transactional(readOnly = true)
  public Page<ForkliftResponse> findByStatus(OperationalStatus status, Pageable pageable) {
    log.info("Filtering active assets by operational status: {}", status);
    return forkliftRepository.findByStatus(status, pageable).map(forkliftMapper::toResponse);
  }

  /**
   * Moves a forklift to a new storage bin location.
   *
   * <p>This operation updates the physical location of the forklift within the warehouse. Both the
   * forklift and the target bin must exist.
   *
   * @param forkliftId the identifier of the forklift to relocate
   * @param locationId the identifier of the destination storage bin
   * @return a DTO representing the updated forklift
   * @throws ForkliftNotFoundException if no forklift with the given ID exists
   */
  @Transactional
  public ForkliftResponse updateForkliftLocation(Long forkliftId, Long locationId) {
    log.info("Moving Forklift ID {} to StorageBin ID {}", forkliftId, locationId);
    Forklift forklift = findEntityById(forkliftId);
    StorageBin newStorageBin = storageBinService.findEntityById(locationId);

    forklift.setCurrentStorageBin(newStorageBin);
    Forklift updatedForklift = forkliftRepository.save(forklift);

    log.debug("Update successful for Forklift ID {}", forkliftId);
    return forkliftMapper.toResponse(updatedForklift);
  }

  /**
   * Transitions a forklift's operational status (e.g. from {@code OFFLINE} to {@code ACTIVE}).
   *
   * @param forkliftId the identifier of the target forklift
   * @param status the new operational status
   * @return a DTO representing the updated forklift
   * @throws ForkliftNotFoundException if no forklift with the given ID exists
   */
  @Transactional
  public ForkliftResponse updateOperationalStatus(Long forkliftId, OperationalStatus status) {
    log.info("Transitioning Forklift ID {} state to: {}", forkliftId, status);
    Forklift forklift = findEntityById(forkliftId);

    forklift.setStatus(status);
    Forklift updatedForklift = forkliftRepository.save(forklift);

    return forkliftMapper.toResponse(updatedForklift);
  }

  /**
   * Internal domain-level lookup: retrieves the managed {@link Forklift} entity by its ID.
   *
   * <p>This method is exposed to other services within the same domain and to the planning service
   * for solution persistence. External consumers receive a DTO; only domain-internal callers should
   * use the entity directly.
   *
   * @param id the forklift's database identifier
   * @return the persistent Forklift entity
   * @throws ForkliftNotFoundException if no entity with that ID exists
   */
  @Transactional(readOnly = true)
  public Forklift findEntityById(Long id) {
    log.info("Fetching Forklift entity with id: {}", id);
    return forkliftRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("Lookup failed: Forklift ID {} not found", id);
              return new ForkliftNotFoundException(id);
            });
  }

  /**
   * Returns a paginated list of raw {@link Forklift} entities for internal domain usage.
   *
   * @param pageable pagination and sorting parameters
   * @return a page of Forklift entities
   */
  @Transactional(readOnly = true)
  public Page<Forklift> findAllEntities(Pageable pageable) {
    log.info("Fetching all managed forklift entities and returning a page");
    return forkliftRepository.findAll(pageable);
  }

  /**
   * Returns all {@link Forklift} entities without pagination for solver consumption.
   *
   * <p><b>Performance note:</b> This method loads the entire fleet into memory and is called during
   * {@code WarehouseDispatcherService.buildCurrentState()}. For large warehouses, consider
   * paginated or selective fetching (addressed in Milestone 2).
   *
   * @return a list of all Forklift entities in the database
   */
  @Transactional(readOnly = true)
  public List<Forklift> findAllEntities() {
    log.info("Fetching all managed forklift entities without pagination");
    return forkliftRepository.findAll();
  }

  /**
   * Persists the solver's assignment results back to the database.
   *
   * <p>Called by {@code WarehouseDispatcherService.saveFinalSolution()} after the Timefold solver
   * produces a solution. This method performs a bulk ID lookup to ensure all referenced forklifts
   * exist <em>before</em> applying any assignment changes. If a forklift was deleted between solver
   * completion and persistence, the operation fails with an {@link IllegalStateException} to
   * prevent partial updates.
   *
   * <p>Each database forklift's transport order list is cleared and repopulated with the
   * solver-determined assignments.
   *
   * @param forklifts the list of solver-state forklifts containing updated order assignments
   * @throws IllegalStateException if one or more forklift IDs from the solver solution no longer
   *     exist in the database (stale data)
   */
  @Transactional
  public void updateAssignedOrders(List<Forklift> forklifts) {
    log.info("Updating assigned transport orders for Forklifts");
    List<Long> ids = forklifts.stream().map(Forklift::getId).toList();
    List<Forklift> databaseForklifts = forkliftRepository.findAllById(ids);
    if (databaseForklifts.size() != ids.size()) {
      throw new IllegalStateException("One or more forklifts not found during assignment update");
    }

    for (Forklift newForklift : forklifts) {
      Forklift databaseForklift = findEntityById(newForklift.getId());

      databaseForklift.getTransportOrders().clear();
      if (newForklift.getTransportOrders() != null) {
        databaseForklift.getTransportOrders().addAll(newForklift.getTransportOrders());
      }
    }
  }
}
