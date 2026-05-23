package com.v1rex.liftnexus.storagebin.repository;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageBinRepository extends JpaRepository<StorageBin, Long> {
  boolean existsByBinCode(String binCode);
}
