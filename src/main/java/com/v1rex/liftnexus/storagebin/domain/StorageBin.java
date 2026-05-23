package com.v1rex.liftnexus.storagebin.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "storage_bin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageBin {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Storage bin should have a bin code")
  @Column(name = "bin_code", nullable = false, unique = true)
  private String binCode;

  @NotNull(message = "Storage bin must have a physical coordinate location")
  @Valid
  @Embedded
  private Coordinate3D coordinate;

  @NotNull(message = "Storage bin must have a zone type")
  @Enumerated(EnumType.STRING)
  @Column(name = "zone_type", nullable = false)
  private ZoneType zoneType;

  @NotNull(message = "Storage bin must have a maximum weight capacity defined")
  @Min(value = 0)
  @Column(name = "max_weight_capacity_kg", nullable = false)
  private Integer maxWeightCapacityKg;
}
