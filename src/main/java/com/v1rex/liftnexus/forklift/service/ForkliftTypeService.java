package com.v1rex.liftnexus.forklift.service;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import com.v1rex.liftnexus.forklift.mapper.ForkliftTypeMapper;
import com.v1rex.liftnexus.forklift.repository.ForkliftTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForkliftTypeService {

  private final ForkliftTypeRepository forkliftTypeRepository;
  private final ForkliftTypeMapper forkliftTypeMapper;

  @Transactional
  public ForkliftTypeResponse createForkliftType(ForkliftTypeRequest request) {
    log.info("Registering new forklift archetype blueprint: {}", request.modelName());

    if (forkliftTypeRepository.existsByModelName(request.modelName())) {
      throw new IllegalStateException(
          "A forklift type model named '" + request.modelName() + "' already exists.");
    }

    ForkliftType forkliftType = forkliftTypeMapper.toEntity(request);
    ForkliftType savedType = forkliftTypeRepository.save(forkliftType);

    return forkliftTypeMapper.toResponse(savedType);
  }

  @Transactional(readOnly = true)
  public ForkliftTypeResponse findById(Long id) {
    return forkliftTypeMapper.toResponse(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public Page<ForkliftTypeResponse> findAll(Pageable pageable) {
    return forkliftTypeRepository.findAll(pageable).map(forkliftTypeMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public ForkliftType findEntityById(Long id) {
    return forkliftTypeRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("ForkliftType with ID " + id + " not found."));
  }
}
