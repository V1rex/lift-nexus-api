package com.v1rex.liftnexus.storagebin.service;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.storagebin.repository.StorageBinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageBinService {

  private final StorageBinRepository storageBinRepository;
  private final StorageBinMapper storageBinMapper;

  // =====================================================================
  // EXTERNAL API BOUNDARY (Returns DTOs to Controllers)
  // =====================================================================
  @Transactional
  public StorageBinResponse createStorageBin(StorageBinRequest request) {
    log.info(
        "Creating storage bin with code: {} at [X:{}, Y:{}, Z:{}]",
        request.binCode(),
        request.coordinate().x(),
        request.coordinate().y(),
        request.coordinate().z());

    if (storageBinRepository.existsByBinCode(request.binCode())) {
      throw new IllegalArgumentException(
          "Storage bin with code " + request.binCode() + " already exists.");
    }

    StorageBin storageBin = storageBinMapper.toEntity(request);
    StorageBin savedBin = storageBinRepository.save(storageBin);

    log.info(
        "Successfully created storage bin with Id: {}, code: {}",
        savedBin.getId(),
        savedBin.getBinCode());
    return storageBinMapper.toResponse(savedBin);
  }

  @Transactional(readOnly = true)
  public StorageBinResponse findById(Long id) {
    return storageBinMapper.toResponse(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public Page<StorageBinResponse> findAll(Pageable pageable) {
    return findAllEntities(pageable).map(storageBinMapper::toResponse);
  }

  // =====================================================================
  // INTERNAL DOMAIN BOUNDARY (Returns Entities to other Services/Timefold)
  // =====================================================================
  public StorageBin findEntityById(Long id) {
    return storageBinRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("Storage bin with id: {} not found.", id);
              return new ResourceNotFoundException("Storage bin with " + id + " not found.");
            });
  }

  public Page<StorageBin> findAllEntities(Pageable pageable) {
    return storageBinRepository.findAll(pageable);
  }
}
