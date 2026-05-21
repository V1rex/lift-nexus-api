package com.v1rex.liftnexus.forklift.repository;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForkliftRepository extends JpaRepository<Forklift, Long> {

  Page<Forklift> findByWeightCapacityGreaterThan(Integer weight, Pageable pageable);
}
