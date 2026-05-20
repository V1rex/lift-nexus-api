package com.v1rex.liftnexus.forklift.service;


import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.location.service.LocationService;
import com.v1rex.liftnexus.forklift.mapper.ForkliftMapper;
import com.v1rex.liftnexus.forklift.repository.ForkliftRepository;
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
    private final LocationService locationService;

    @Transactional
    public ForkliftResponse createForklift(ForkliftRequest request){
        log.info("Creating a Forklift: {}", request);
        Forklift forklift = forkliftMapper.toEntity(request);

        Forklift savedForklift = forkliftRepository.save(forklift);

        log.info("Successfully created Forklift with Id: {}", savedForklift.getId());
        return forkliftMapper.toResponse(savedForklift);
    }

    @Transactional(readOnly = true)
    public ForkliftResponse findById(Long id) {
        return forkliftMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<ForkliftResponse> findAll(Pageable pageable) {
         return findAllEntities(pageable).map(forkliftMapper::toResponse);
     }

     @Transactional(readOnly = true)
     public Page<ForkliftResponse> findWithCapacityGreaterThan(
                                                            Integer weightCapacity,
                                                            Pageable pageable
     ){
         return findEntitiesWithCapacityGreaterThan(weightCapacity, pageable).map(forkliftMapper::toResponse);
     }

     @Transactional
     public ForkliftResponse updateForkliftLocation(Long forkLiftId, Long locationId){
        log.info("Moving Forklift ID {} to Location ID {}", forkLiftId, locationId);
        Forklift forklift = findEntityById(forkLiftId);
        Location newLocation = locationService.findEntityById(locationId);

        forklift.setCurrentLocation(newLocation);

        Forklift updatedForklift = forkliftRepository.save(forklift);

        log.debug("Update successful for Forklift ID {}", forkLiftId);
        return forkliftMapper.toResponse(updatedForklift);
     }

     public Forklift findEntityById(Long id) {
        return forkliftRepository.findById(id)
                .orElseThrow(() ->{

                    log.warn("Lookup failed: Forklift ID {} not found", id);
                    return new ResourceNotFoundException("Forklift with " + id + " not found.");
                } );
    }

     public Page<Forklift> findAllEntities(Pageable pageable){
     return forkliftRepository.findAll(pageable);
 }

     public Page<Forklift> findEntitiesWithCapacityGreaterThan(
                                                                Integer weightCapacity,
                                                               Pageable pageable){

         return forkliftRepository.findByWeightCapacityGreaterThan(weightCapacity, pageable);

     }
}
