package com.v1rex.liftnexus.loadunit.service;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
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
      throw new IllegalArgumentException(
          "Load Unit with tracking code '" + request.trackingCode() + "' already exists.");
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

  @Transactional(readOnly = true)
  public LoadUnitResponse findById(Long id) {
    return loadUnitMapper.toResponse(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public LoadUnitResponse findByTrackingCode(String trackingCode) {
    return loadUnitMapper.toResponse(findEntityByTrackingCode(trackingCode));
  }

  @Transactional(readOnly = true)
  public Page<LoadUnitResponse> findAll(Pageable pageable) {
    return findAllEntities(pageable).map(loadUnitMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<LoadUnitResponse> findByStatus(LoadUnitStatus status, Pageable pageable) {
    return findEntitiesByStatus(status, pageable).map(loadUnitMapper::toResponse);
  }

  // =====================================================================
  // INTERNAL DOMAIN BOUNDARY (Returns Entities to other Services/Timefold)
  // =====================================================================
  public LoadUnit findEntityById(Long id) {
    return loadUnitRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("Load unit with id: {} not found.", id);
              return new ResourceNotFoundException("Load Unit with ID " + id + " not found.");
            });
  }

  public LoadUnit findEntityByTrackingCode(String trackingCode) {
    return loadUnitRepository
        .findByTrackingCode(trackingCode)
        .orElseThrow(
            () -> {
              log.warn("Load unit with tracking code: {} not found.", trackingCode);
              return new ResourceNotFoundException(
                  "Load Unit with tracking code '" + trackingCode + "' not found.");
            });
  }

  public Page<LoadUnit> findAllEntities(Pageable pageable) {
    return loadUnitRepository.findAll(pageable);
  }

  public Page<LoadUnit> findEntitiesByStatus(LoadUnitStatus status, Pageable pageable) {
    return loadUnitRepository.findByStatus(status, pageable);
  }
}
