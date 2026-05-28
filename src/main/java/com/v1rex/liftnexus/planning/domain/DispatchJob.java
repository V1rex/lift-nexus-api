package com.v1rex.liftnexus.planning.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchJob {

  @Id private UUID id;

  @NonNull
  @Enumerated(EnumType.STRING)
  private JobStatus status;

  @NonNull private Instant createdAt;

  private Instant completedAt;

  private String finalScore;
}
