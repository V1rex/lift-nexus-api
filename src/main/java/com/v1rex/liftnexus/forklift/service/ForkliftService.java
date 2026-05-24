package com.v1rex.liftnexus.forklift.service;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ForkliftService {

  private final ForkliftRepository forkliftRepository;
  private final ForkliftMapper forkliftMapper;
  private final ForkliftTypeService forkliftTypeService;
  private final StorageBinService storageBinService;

  @Transactional
  public ForkliftResponse createForklift(ForkliftRequest request) {
    log.info("Provisioning new warehouse asset with fleet number: {}", request.fleetNumber());

    if (forkliftRepository.existsByFleetNumber(request.fleetNumber())) {
      throw new IllegalStateException(
          "A forklift with fleet number '" + request.fleetNumber() + "' already exists.");
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

  @Transactional(readOnly = true)
  public ForkliftResponse findById(Long id) {
    return forkliftMapper.toResponse(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public Page<ForkliftResponse> findAll(Pageable pageable) {
    return forkliftRepository.findAll(pageable).map(forkliftMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<ForkliftResponse> findWithCapacityGreaterThan(
      Integer minCapacity, Pageable pageable) {
    log.info("Searching assets matching minimum operational lifting capacity: {}kg", minCapacity);
    return forkliftRepository
        .findByForkliftType_MaxCapacityKgGreaterThanEqual(minCapacity, pageable)
        .map(forkliftMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<ForkliftResponse> findByStatus(OperationalStatus status, Pageable pageable) {
    log.info("Filtering active assets by operational status: {}", status);
    return forkliftRepository.findByStatus(status, pageable).map(forkliftMapper::toResponse);
  }

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

  @Transactional
  public ForkliftResponse updateOperationalStatus(Long forkliftId, OperationalStatus status) {
    log.info("Transitioning Forklift ID {} state to: {}", forkliftId, status);
    Forklift forklift = findEntityById(forkliftId);

    forklift.setStatus(status);
    Forklift updatedForklift = forkliftRepository.save(forklift);

    return forkliftMapper.toResponse(updatedForklift);
  }

  @Transactional(readOnly = true)
  public Forklift findEntityById(Long id) {
    return forkliftRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("Lookup failed: Forklift ID {} not found", id);
              return new ResourceNotFoundException("Forklift with " + id + " not found.");
            });
  }

  @Transactional(readOnly = true)
  public List<Forklift> findAllEntitiesForPlanning() {
    log.info("Fetching all managed forklift assets for Timefold optimization engine execution");
    return forkliftRepository.findAll();
  }
}
