package com.v1rex.liftnexus.forklift.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@PlanningEntity
@Entity
@Table(name = "forklifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forklift {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(name = "fleet_number", nullable = false, unique = true)
  private String fleetNumber;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "forklift_type_id", nullable = false)
  private ForkliftType forkliftType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "current_storage_bin_id")
  private StorageBin currentStorageBin;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "operational_status", nullable = false)
  private OperationalStatus status = OperationalStatus.OFFLINE;

  @Builder.Default
  @Column(name = "current_battery_percentage", nullable = false)
  private Double currentBatteryPercentage = 100.0;

  @PlanningListVariable(valueRangeProviderRefs = "taskPoolRange")
  @OneToMany(mappedBy = "assignedForklift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<TransportOrder> transportOrders = new ArrayList<>();
}
