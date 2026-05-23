package com.v1rex.liftnexus.storagebin.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate3D {
  // Represents the Aisle
  private int x;

  // Represents the Bay
  private int y;

  // Represents the Tier (Height)
  private int z;

  /** Calculates the Manhattan distance using the default vertical penalty factor of 2.5. */
  public double calculateDistance(Coordinate3D other) {
    return calculateDistance(other, 2.5);
  }

  /**
   * Calculates the Manhattan distance with a custom vertical penalty factor. Useful if certain
   * warehouse areas have faster/slower vertical lifts.
   */
  public double calculateDistance(Coordinate3D other, double zWeightPenalty) {
    int dx = Math.abs(this.x - other.x);
    int dy = Math.abs(this.y - other.y);
    int dz = Math.abs(this.z - other.z);

    return dx + dy + (dz * zWeightPenalty);
  }
}
