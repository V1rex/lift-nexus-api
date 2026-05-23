package com.v1rex.liftnexus.loadunit.repository;

import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoadUnitRepository extends JpaRepository<LoadUnit, Long> {

  boolean existsByTrackingCode(String trackingCode);

  Optional<LoadUnit> findByTrackingCode(String trackingCode);

  Page<LoadUnit> findByStatus(LoadUnitStatus status, Pageable pageable);
}
