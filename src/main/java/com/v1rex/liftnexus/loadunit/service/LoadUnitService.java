package com.v1rex.liftnexus.loadunit.service;

import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
import com.v1rex.liftnexus.loadunit.exception.LoadUnitNotFoundException;
import com.v1rex.liftnexus.loadunit.exception.LoadUnitTrackingCodeExistsException;
import com.v1rex.liftnexus.loadunit.mapper.LoadUnitMapper;
import com.v1rex.liftnexus.loadunit.repository.LoadUnitRepository;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing {@link LoadUnit} entities.
 *
 * <p>This class provides two tiers of access:
 *
 * <ul>
 *   <li><b>External API Boundary</b> – Methods that return DTOs ({@link LoadUnitResponse}) to
 *       controllers and external callers. These methods handle validation, business logic, and
 *       mapping.
 *   <li><b>Internal Domain Boundary</b> – Methods that return domain entities ({@link LoadUnit})
 *       for use by other services, internal components, or the Timefold solver, bypassing DTO
 *       conversion.
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoadUnitService {

  private final LoadUnitRepository loadUnitRepository;
  private final LoadUnitMapper loadUnitMapper;

  private final StorageBinService storageBinService;

  // =====================================================================
  // EXTERNAL API BOUNDARY (Returns DTOs to Controllers)
  // =====================================================================

  /**
   * Creates a new load unit and persists it to the database.
   *
   * <p>Before persisting, this method validates that the provided {@code trackingCode} is unique.
   * If a storage bin ID is supplied, the load unit will be assigned to the referenced bin (the bin
   * must already exist).
   *
   * @param request the DTO containing the load unit details (tracking code, weight, optional
   *     storage bin ID, etc.)
   * @return a {@link LoadUnitResponse} representing the newly created and saved load unit
   * @throws LoadUnitTrackingCodeExistsException if a load unit with the same tracking code already
   *     exists
   */
  @Transactional
  public LoadUnitResponse createLoadUnit(LoadUnitRequest request) {
    log.info(
        "Creating load unit with tracking code: {} and weight: {}kg",
        request.trackingCode(),
        request.weightKg());

    if (loadUnitRepository.existsByTrackingCode(request.trackingCode())) {
      log.warn(
          "Creation failed: Load unit with tracking code {} already exists",
          request.trackingCode());
      throw new LoadUnitTrackingCodeExistsException(request.trackingCode());
    }

    StorageBin assignedBin = null;
    if (request.currentStorageBinId() != null) {
      assignedBin = storageBinService.findEntityById(request.currentStorageBinId());
    }

    LoadUnit loadUnit = loadUnitMapper.toEntity(request, assignedBin);
    LoadUnit savedUnit = loadUnitRepository.save(loadUnit);

    log.info(
        "Successfully created load unit with Id: {}, tracking code: {}",
        savedUnit.getId(),
        savedUnit.getTrackingCode());
    return loadUnitMapper.toResponse(savedUnit);
  }

  /**
   * Retrieves a load unit by its database ID and returns it as a DTO.
   *
   * @param id the primary key of the load unit
   * @return a {@link LoadUnitResponse} for the matching load unit
   * @throws LoadUnitNotFoundException if no load unit exists with the given {@code id}
   */
  @Transactional(readOnly = true)
  public LoadUnitResponse findById(Long id) {
    return loadUnitMapper.toResponse(findEntityById(id));
  }

  /**
   * Retrieves a load unit by its unique tracking code and returns it as a DTO.
   *
   * @param trackingCode the unique tracking code to search for
   * @return a {@link LoadUnitResponse} for the matching load unit
   * @throws LoadUnitNotFoundException if no load unit exists with the given {@code trackingCode}
   */
  @Transactional(readOnly = true)
  public LoadUnitResponse findByTrackingCode(String trackingCode) {
    return loadUnitMapper.toResponse(findEntityByTrackingCode(trackingCode));
  }

  /**
   * Returns a paginated list of all load units as DTOs.
   *
   * @param pageable pagination and sorting configuration
   * @return a {@link Page} of {@link LoadUnitResponse}
   */
  @Transactional(readOnly = true)
  public Page<LoadUnitResponse> findAll(Pageable pageable) {
    return findAllEntities(pageable).map(loadUnitMapper::toResponse);
  }

  /**
   * Returns a paginated list of load units filtered by the given {@link LoadUnitStatus} as DTOs.
   *
   * @param status the status to filter by (e.g. {@code AVAILABLE}, {@code RESERVED}, etc.)
   * @param pageable pagination and sorting configuration
   * @return a {@link Page} of {@link LoadUnitResponse} matching the specified status
   */
  @Transactional(readOnly = true)
  public Page<LoadUnitResponse> findByStatus(LoadUnitStatus status, Pageable pageable) {
    return findEntitiesByStatus(status, pageable).map(loadUnitMapper::toResponse);
  }

  // =====================================================================
  // INTERNAL DOMAIN BOUNDARY (Returns Entities to other Services/Timefold)
  // =====================================================================

  /**
   * Finds a load unit entity by its database ID.
   *
   * <p>This method is intended for internal use by other services, domain components, or the
   * Timefold solver that require direct access to the domain entity rather than a DTO.
   *
   * @param id the primary key of the load unit
   * @return the {@link LoadUnit} entity
   * @throws LoadUnitNotFoundException if no load unit exists with the given {@code id}
   */
  public LoadUnit findEntityById(Long id) {
    return loadUnitRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("Load unit with id: {} not found.", id);
              return new LoadUnitNotFoundException(id);
            });
  }

  /**
   * Finds a load unit entity by its unique tracking code.
   *
   * <p>This method is intended for internal use by other services, domain components, or the
   * Timefold solver that require direct access to the domain entity rather than a DTO.
   *
   * @param trackingCode the unique tracking code to search for
   * @return the {@link LoadUnit} entity
   * @throws LoadUnitNotFoundException if no load unit exists with the given {@code trackingCode}
   */
  public LoadUnit findEntityByTrackingCode(String trackingCode) {
    return loadUnitRepository
        .findByTrackingCode(trackingCode)
        .orElseThrow(
            () -> {
              log.warn("Load unit with tracking code: {} not found.", trackingCode);
              return new LoadUnitNotFoundException(trackingCode);
            });
  }

  /**
   * Returns a paginated list of all load unit entities.
   *
   * <p>This method is intended for internal use by other services, domain components, or the
   * Timefold solver that require direct access to the domain entity rather than a DTO.
   *
   * @param pageable pagination and sorting configuration
   * @return a {@link Page} of {@link LoadUnit} entities
   */
  public Page<LoadUnit> findAllEntities(Pageable pageable) {
    return loadUnitRepository.findAll(pageable);
  }

  /**
   * Returns a paginated list of load unit entities filtered by the given {@link LoadUnitStatus}.
   *
   * <p>This method is intended for internal use by other services, domain components, or the
   * Timefold solver that require direct access to the domain entity rather than a DTO.
   *
   * @param status the status to filter by
   * @param pageable pagination and sorting configuration
   * @return a {@link Page} of {@link LoadUnit} entities matching the specified status
   */
  public Page<LoadUnit> findEntitiesByStatus(LoadUnitStatus status, Pageable pageable) {
    return loadUnitRepository.findByStatus(status, pageable);
  }
}
