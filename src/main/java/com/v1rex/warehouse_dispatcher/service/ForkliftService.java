package com.v1rex.warehouse_dispatcher.service;


import com.v1rex.warehouse_dispatcher.domain.Forklift;
import com.v1rex.warehouse_dispatcher.domain.Location;
import com.v1rex.warehouse_dispatcher.dto.ForkliftRequest;
import com.v1rex.warehouse_dispatcher.dto.ForkliftResponse;
import com.v1rex.warehouse_dispatcher.exceptions.ResourceNotFoundException;
import com.v1rex.warehouse_dispatcher.mapper.ForkliftMapper;
import com.v1rex.warehouse_dispatcher.repository.ForkliftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class ForkliftService {
    private final ForkliftRepository forkliftRepository;
    private final ForkliftMapper forkliftMapper;
    private final LocationService locationService;

    @Transactional
    public ForkliftResponse createForklift(ForkliftRequest request){
        Forklift forklift = forkliftMapper.toEntity(request);

        Forklift savedForklift = forkliftRepository.save(forklift);

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
        Forklift forklift = findEntityById(forkLiftId);
        Location newLocation = locationService.findEntityById(locationId);

        forklift.setCurrentLocation(newLocation);

        Forklift updatedForklift = forkliftRepository.save(forklift);

        return forkliftMapper.toResponse(updatedForklift);
     }

     public Forklift findEntityById(Long id) {
        return forkliftRepository.findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Forklift with " + id + " not found.") );
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
