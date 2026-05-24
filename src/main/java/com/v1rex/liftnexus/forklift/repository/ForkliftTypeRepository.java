package com.v1rex.liftnexus.forklift.repository;

import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForkliftTypeRepository extends JpaRepository<ForkliftType, Long> {
  Optional<ForkliftType> findByModelName(String modelName);

  boolean existsByModelName(String modelName);
}
