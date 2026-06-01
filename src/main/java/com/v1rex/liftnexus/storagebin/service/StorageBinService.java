package com.v1rex.liftnexus.storagebin.service;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.exception.StorageBinCodeExistsException;
import com.v1rex.liftnexus.storagebin.exception.StorageBinNotFoundException;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.storagebin.repository.StorageBinRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for {@link StorageBin} domain operations.
 *
 * <p>This class provides a clear separation between <b>external API</b> methods (returning DTOs to
 * controllers) and <b>internal domain</b> methods (returning entities to other services or the
 * Timefold solver). This dual-boundary pattern ensures that external clients receive decoupled
 * response objects, while internal consumers have full access to the domain model for complex
 * operations like constraint-based optimisation.
 *
 * <p>All public methods are {@link Transactional @Transactional} to guarantee data consistency.
 *
 * @see StorageBinRepository
 * @see StorageBinMapper
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StorageBinService {

  private final StorageBinRepository storageBinRepository;
  private final StorageBinMapper storageBinMapper;

  // =====================================================================
  // EXTERNAL API BOUNDARY (Returns DTOs to Controllers)
  // =====================================================================

  /**
   * Creates a new storage bin and returns its DTO representation.
   *
   * <p>Before persisting, this method validates that the supplied {@code binCode} is unique. If a
   * storage bin with the same code already exists, a {@link StorageBinCodeExistsException} is
   * thrown.
   *
   * @param request the input data containing the bin code and its spatial coordinates
   * @return a {@link StorageBinResponse} representing the newly persisted storage bin
   * @throws StorageBinCodeExistsException if a storage bin with the given {@code binCode} already
   *     exists in the database
   */
  @Transactional
  public StorageBinResponse createStorageBin(StorageBinRequest request) {
    log.info(
        "Creating storage bin with code: {} at [X:{}, Y:{}, Z:{}]",
        request.binCode(),
        request.coordinate().x(),
        request.coordinate().y(),
        request.coordinate().z());

    if (storageBinRepository.existsByBinCode(request.binCode())) {
      throw new StorageBinCodeExistsException(request.binCode());
    }

    StorageBin storageBin = storageBinMapper.toEntity(request);
    StorageBin savedBin = storageBinRepository.save(storageBin);

    log.info(
        "Successfully created storage bin with Id: {}, code: {}",
        savedBin.getId(),
        savedBin.getBinCode());
    return storageBinMapper.toResponse(savedBin);
  }

  /**
   * Retrieves a storage bin by its unique identifier and returns its DTO representation.
   *
   * <p>If no storage bin exists with the given {@code id}, a {@link StorageBinNotFoundException} is
   * thrown.
   *
   * @param id the storage bin's primary key
   * @return a {@link StorageBinResponse} for the matching storage bin
   * @throws StorageBinNotFoundException if no storage bin is found for the given {@code id}
   * @see #findEntityById(Long)
   */
  @Transactional(readOnly = true)
  public StorageBinResponse findById(Long id) {
    return storageBinMapper.toResponse(findEntityById(id));
  }

  /**
   * Retrieves a paginated list of all storage bins, mapped to their DTO representations.
   *
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link StorageBinResponse} objects
   * @see #findAllEntities(Pageable)
   */
  @Transactional(readOnly = true)
  public Page<StorageBinResponse> findAll(Pageable pageable) {
    return findAllEntities(pageable).map(storageBinMapper::toResponse);
  }

  // =====================================================================
  // INTERNAL DOMAIN BOUNDARY (Returns Entities to other Services/Timefold)
  // =====================================================================

  /**
   * Finds a {@link StorageBin} entity by its identifier.
   *
   * <p>This is an <b>internal</b> method intended for use by other services or the Timefold solver
   * that require access to the full domain model. It throws a {@link StorageBinNotFoundException}
   * when the entity is not found.
   *
   * @param id the storage bin's primary key
   * @return the {@link StorageBin} entity
   * @throws StorageBinNotFoundException if no storage bin exists for the given {@code id}
   */
  @Transactional(readOnly = true)
  public StorageBin findEntityById(Long id) {
    log.info("Fetching storage bin entity with id: {}", id);
    return storageBinRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("Storage bin with id: {} not found.", id);
              return new StorageBinNotFoundException(id);
            });
  }

  /**
   * Retrieves a paginated list of all {@link StorageBin} entities.
   *
   * <p>This is an <b>internal</b> method that returns full domain objects, suitable for batch
   * processing or solver input.
   *
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link StorageBin} entities
   */
  @Transactional(readOnly = true)
  public Page<StorageBin> findAllEntities(Pageable pageable) {
    log.info("Fetching all managed storage bin entities with pagination");
    return storageBinRepository.findAll(pageable);
  }

  /**
   * Retrieves all {@link StorageBin} entities without pagination.
   *
   * <p>This is an <b>internal</b> method that should be used with care when the total number of
   * bins is expected to be small, or when the caller intentionally loads the full collection (e.g.,
   * seeding the Timefold solver).
   *
   * @return an unmodifiable-style {@link List} of all {@link StorageBin} entities
   */
  @Transactional(readOnly = true)
  public List<StorageBin> findAllEntities() {
    log.info("Fetching all managed storage bin entities without pagination");
    return storageBinRepository.findAll();
  }
}
