package com.v1rex.liftnexus.forklift.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "forklift_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForkliftType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(name = "model_name", nullable = false, unique = true)
  private String modelName;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "equipment_type", nullable = false)
  private EquipmentType equipmentType;

  @Positive
  @Column(name = "max_capacity_kg", nullable = false)
  private Integer maxCapacityKg;

  @Positive
  @Column(name = "total_battery_capacity_kwh", nullable = false)
  private Double totalBatteryCapacitykWh;

  @Positive
  @Column(name = "base_energy_consumption_per_meter", nullable = false)
  private Double baseEnergyConsumptionPerMeter;
}
