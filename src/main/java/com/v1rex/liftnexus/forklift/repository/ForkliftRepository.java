package com.v1rex.liftnexus.forklift.repository;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ForkliftRepository extends JpaRepository<Forklift, Long> {

  boolean existsByFleetNumber(String fleetNumber);

  @EntityGraph(attributePaths = {"forkliftType", "currentStorageBin"})
  Optional<Forklift> findById(Long id);

  @Override
  @EntityGraph(attributePaths = {"forkliftType", "currentStorageBin"})
  Page<Forklift> findAll(Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"forkliftType", "currentStorageBin"})
  List<Forklift> findAll();

  @EntityGraph(attributePaths = {"forkliftType", "currentStorageBin"})
  Page<Forklift> findByStatus(OperationalStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"forkliftType", "currentStorageBin"})
  @Query(
      """
        SELECT f FROM Forklift f
        JOIN f.forkliftType ft
        WHERE ft.maxCapacityKg >= :minCapacity
    """)
  Page<Forklift> findByForkliftType_MaxCapacityKgGreaterThanEqual(
      @Param("minCapacity") Integer minCapacity, Pageable pageable);
}
