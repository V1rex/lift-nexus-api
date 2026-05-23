package com.v1rex.liftnexus.storagebin.service;

import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.LocationRequest;
import com.v1rex.liftnexus.storagebin.dto.LocationResponse;
import com.v1rex.liftnexus.storagebin.mapper.LocationMapper;
import com.v1rex.liftnexus.storagebin.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

  private final LocationRepository locationRepository;

  private final LocationMapper locationMapper;

  @Transactional
  public LocationResponse createLocation(LocationRequest request) {
    log.info(
        "Creating storagebin with latitude: {} and longitude: {}",
        request.latitude(),
        request.longitude());
    StorageBin storageBin = locationMapper.toEntity(request);

    StorageBin savedStorageBin = locationRepository.save(storageBin);

    log.info(
        "Successfully creation storagebin with Id: {} latitude: {} and longitude: {}",
        savedStorageBin.getId(),
        savedStorageBin.getLatitude(),
        savedStorageBin.getLongitude());

    return locationMapper.toResponse(savedStorageBin);
  }

  @Transactional(readOnly = true)
  public LocationResponse findById(Long id) {
    return locationMapper.toResponse(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public Page<LocationResponse> findAll(Pageable pageable) {
    return findAllEntities(pageable).map(locationMapper::toResponse);
  }

  public StorageBin findEntityById(Long id) {
    return locationRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("StorageBin with id: {} not found.", id);
              return new ResourceNotFoundException("StorageBin  with " + id + " not found.");
            });
  }

  public Page<StorageBin> findAllEntities(Pageable pageable) {
    return locationRepository.findAll(pageable);
  }
}
