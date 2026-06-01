package com.v1rex.liftnexus.planning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "dispatch_jobs")
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
