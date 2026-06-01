package com.v1rex.liftnexus.forklift.service;

import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import com.v1rex.liftnexus.forklift.exception.ForkliftTypeNameExistsException;
import com.v1rex.liftnexus.forklift.exception.ForkliftTypeNotFoundException;
import com.v1rex.liftnexus.forklift.mapper.ForkliftTypeMapper;
import com.v1rex.liftnexus.forklift.repository.ForkliftTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing {@link ForkliftType} entities.
 *
 * <p>Provides transactional operations for creating, retrieving, and querying forklift types.
 * Enforces uniqueness constraints on model names and uses a mapper for entity-to-DTO conversion.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ForkliftTypeService {

  private final ForkliftTypeRepository forkliftTypeRepository;
  private final ForkliftTypeMapper forkliftTypeMapper;

  /**
   * Creates a new forklift type after validating that the model name is unique.
   *
   * @param request the DTO containing forklift type details (model name, etc.)
   * @return a {@link ForkliftTypeResponse} representing the persisted entity
   * @throws ForkliftTypeNameExistsException if a type with the given model name already exists
   */
  @Transactional
  public ForkliftTypeResponse createForkliftType(ForkliftTypeRequest request) {
    log.info("Registering new forklift archetype blueprint: {}", request.modelName());

    if (forkliftTypeRepository.existsByModelName(request.modelName())) {
      throw new ForkliftTypeNameExistsException(request.modelName());
    }

    ForkliftType forkliftType = forkliftTypeMapper.toEntity(request);
    ForkliftType savedType = forkliftTypeRepository.save(forkliftType);

    return forkliftTypeMapper.toResponse(savedType);
  }

  /**
   * Retrieves a forklift type by its unique identifier.
   *
   * @param id the primary key of the forklift type
   * @return a {@link ForkliftTypeResponse} representing the found entity
   * @throws ForkliftTypeNotFoundException if no type exists with the given id
   */
  @Transactional(readOnly = true)
  public ForkliftTypeResponse findById(Long id) {
    return forkliftTypeMapper.toResponse(findEntityById(id));
  }

  /**
   * Retrieves a paginated list of all forklift types.
   *
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link ForkliftTypeResponse} DTOs
   */
  @Transactional(readOnly = true)
  public Page<ForkliftTypeResponse> findAll(Pageable pageable) {
    return forkliftTypeRepository.findAll(pageable).map(forkliftTypeMapper::toResponse);
  }

  /**
   * Finds the underlying {@link ForkliftType} entity by its id, throwing if not found.
   *
   * <p>This is an internal helper used by other service methods that need access to the entity
   * object rather than its DTO representation.
   *
   * @param id the primary key of the forklift type
   * @return the {@link ForkliftType} entity
   * @throws ForkliftTypeNotFoundException if no type exists with the given id
   */
  @Transactional(readOnly = true)
  public ForkliftType findEntityById(Long id) {
    return forkliftTypeRepository
        .findById(id)
        .orElseThrow(() -> new ForkliftTypeNotFoundException(id));
  }
}
