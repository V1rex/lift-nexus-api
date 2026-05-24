package com.v1rex.liftnexus.transportorder.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@PlanningEntity
@Entity
@Table(name = "transport_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "load_unit_id", nullable = false)
  private LoadUnit targetLoadUnit;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_bin_id", nullable = false)
  private StorageBin targetBin;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_bin_id", nullable = false)
  private StorageBin sourceBin;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "required_equipment", nullable = false)
  private EquipmentType requiredEquipment = EquipmentType.STANDARD;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TransportOrderStatus status = TransportOrderStatus.OPEN;

  @InverseRelationShadowVariable(sourceVariableName = "transportOrders")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "forklift_id")
  @JsonIgnore
  private Forklift assignedForklift;

  @PlanningPin
  public boolean isPinned() {
    return status == TransportOrderStatus.IN_PROGRESS
        || status == TransportOrderStatus.COMPLETED
        || status == TransportOrderStatus.FAILED;
  }
}
