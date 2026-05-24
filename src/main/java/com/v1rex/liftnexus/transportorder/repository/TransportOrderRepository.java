package com.v1rex.liftnexus.transportorder.repository;

import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransportOrderRepository extends JpaRepository<TransportOrder, Long> {

  List<TransportOrder> findByAssignedForkliftIsNull();

  Page<TransportOrder> findByStatus(TransportOrderStatus status, Pageable pageable);

  Page<TransportOrder> findByTargetLoadUnit_WeightKgGreaterThan(Integer weight, Pageable pageable);

  @Query(
      """
        SELECT to FROM TransportOrder to
        JOIN to.targetLoadUnit lu
        WHERE (:status IS NULL OR to.status = :status)
        AND (:minWeight IS NULL OR lu.weightKg >= :minWeight)
    """)
  Page<TransportOrder> searchOrders(
      @Param("status") TransportOrderStatus status,
      @Param("minWeight") Integer minWeight,
      Pageable pageable);
}
