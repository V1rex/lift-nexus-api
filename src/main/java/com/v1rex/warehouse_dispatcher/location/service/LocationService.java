package com.v1rex.warehouse_dispatcher.location.service;

import com.v1rex.warehouse_dispatcher.location.domain.Location;
import com.v1rex.warehouse_dispatcher.location.dto.LocationRequest;
import com.v1rex.warehouse_dispatcher.location.dto.LocationResponse;
import com.v1rex.warehouse_dispatcher.common.exception.ResourceNotFoundException;
import com.v1rex.warehouse_dispatcher.location.mapper.LocationMapper;
import com.v1rex.warehouse_dispatcher.location.repository.LocationRepository;
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
    public LocationResponse createLocation(LocationRequest request){
        log.info("Creating location with latitude: {} and longitude: {}", request.latitude(), request.longitude());
        Location location = locationMapper.toEntity(request);

        Location savedLocation = locationRepository.save(location);


        log.info("Successfully creation location with Id: {} latitude: {} and longitude: {}",
                savedLocation.getId(),
                savedLocation.getLatitude(),
                savedLocation.getLongitude());


        return locationMapper.toResponse(savedLocation);
    }

    @Transactional(readOnly=true )
    public LocationResponse findById(Long id) {
        return locationMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> findAll(Pageable pageable) {
        return findAllEntities(pageable).map(locationMapper::toResponse);
    }


    public Location findEntityById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() ->{
                    log.warn("Location with id: {} not found.", id);
                    return new ResourceNotFoundException("Location  with "
                            + id + " not found.");});
    }

    public Page<Location> findAllEntities(Pageable pageable){
        return locationRepository.findAll(pageable);
    }

}
