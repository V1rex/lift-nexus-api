package com.v1rex.liftnexus.forklift.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@PlanningEntity
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forklift {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Min(value = 1, message = "Weight must be greater than 0")
  @Column(
      name = "weight_capacity",
      nullable = false,
      columnDefinition = "integer check (weight_capacity >0)")
  private Integer weightCapacity;

  @PlanningListVariable(valueRangeProviderRefs = "taskPoolRange")
  @OneToMany(mappedBy = "assignedForklift", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<TransportOrder> transportOrders = new ArrayList<>();

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "equipment_type", nullable = false)
  private EquipmentType equipmentType = EquipmentType.STANDARD;

  @ManyToOne
  @JoinColumn(name = "current_location_id")
  private StorageBin currentStorageBin;
}
