package com.v1rex.liftnexus.location.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Float latitude;

  @Column(nullable = false)
  private Float longitude;

  public double distanceTo(Location other) {
    double dx = this.latitude - other.latitude;
    double dy = this.longitude - other.longitude;
    return Math.sqrt(dx * dx + dy * dy);
  }
}
