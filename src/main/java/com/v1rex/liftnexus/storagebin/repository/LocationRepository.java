package com.v1rex.liftnexus.storagebin.repository;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<StorageBin, Long> {}
