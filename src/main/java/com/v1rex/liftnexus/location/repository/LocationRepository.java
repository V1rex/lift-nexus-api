package com.v1rex.liftnexus.location.repository;

import com.v1rex.liftnexus.location.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {}
