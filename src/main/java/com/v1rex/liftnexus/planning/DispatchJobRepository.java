package com.v1rex.liftnexus.planning;

import com.v1rex.liftnexus.planning.domain.DispatchJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DispatchJobRepository extends JpaRepository<DispatchJob, UUID> {}
