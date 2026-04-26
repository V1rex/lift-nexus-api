package com.v1rex.warehouse_dispatcher.service;

import com.v1rex.warehouse_dispatcher.domain.Location;
import com.v1rex.warehouse_dispatcher.dto.LocationRequest;
import com.v1rex.warehouse_dispatcher.dto.LocationResponse;
import com.v1rex.warehouse_dispatcher.exceptions.ResourceNotFoundException;
import com.v1rex.warehouse_dispatcher.mapper.LocationMapper;
import com.v1rex.warehouse_dispatcher.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    private final LocationMapper locationMapper;

    @Transactional
    public LocationResponse createLocation(LocationRequest request){
        Location location = locationMapper.toEntity(request);

        Location savedForklift = locationRepository.save(location);

        return locationMapper.toResponse(savedForklift);
    }

    @Transactional(readOnly=true )
    public LocationResponse findById(Long id) {
        return locationMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> findAll(Pageable pageable) {
        return findAllEntities(pageable).map(locationMapper::toResponse);
    }


    protected Location findEntityById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location  with "
                + id + " not found."));
    }

    protected Page<Location> findAllEntities(Pageable pageable){
        return locationRepository.findAll(pageable);
    }

}
