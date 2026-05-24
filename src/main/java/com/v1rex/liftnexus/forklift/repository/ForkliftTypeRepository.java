package com.v1rex.liftnexus.forklift.repository;

import com.v1rex.liftnexus.forklift.domain.ForkliftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForkliftTypeRepository extends JpaRepository<ForkliftType, Long> {
    Optional<ForkliftType> findByModelName(String modelName);
    boolean existsByModelName(String modelName);
}