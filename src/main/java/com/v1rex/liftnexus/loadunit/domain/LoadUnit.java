package com.v1rex.liftnexus.loadunit.domain;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "load_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadUnit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Tracking code must be provided")
  @Column(nullable = false, updatable = false, unique = true)
  private String trackingCode;

  @Min(value = 0, message = "Weight cannot be negative")
  @Column(nullable = false, updatable = false)
  private int weightKg;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LoadUnitStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "current_storage_bin_id")
  private StorageBin currentBin;

  @Version private Long version;
}
