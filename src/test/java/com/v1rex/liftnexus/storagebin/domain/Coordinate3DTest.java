package com.v1rex.liftnexus.storagebin.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Coordinate3D Domain Unit Tests")
class Coordinate3DTest {

  @Test
  @DisplayName("Should correctly calculate Manhattan distance on a flat plane (same tier)")
  void shouldCalculateFlatManhattanDistance() {
    Coordinate3D binA = new Coordinate3D(2, 5, 1);
    Coordinate3D binB = new Coordinate3D(5, 10, 1);

    double distance = binA.calculateDistance(binB);

    assertThat(distance).isEqualTo(8.0);
  }

  @Test
  @DisplayName("Should apply default vertical penalty factor when transitioning across tiers")
  void shouldApplyVerticalPenalty() {
    Coordinate3D groundBin = new Coordinate3D(2, 5, 1);
    Coordinate3D highBin = new Coordinate3D(2, 5, 4);

    double distance = groundBin.calculateDistance(highBin);

    assertThat(distance).isEqualTo(7.5);
  }

  @Test
  @DisplayName(
      "Should accept custom vertical penalties for specialized material handling equipment")
  void shouldAcceptCustomPenalty() {
    Coordinate3D groundBin = new Coordinate3D(1, 1, 1);
    Coordinate3D highBin = new Coordinate3D(1, 1, 3);
    double fastLiftTruckPenalty = 1.2;

    double distance = groundBin.calculateDistance(highBin, fastLiftTruckPenalty);

    assertThat(distance).isEqualTo(2.4, within(0.01));
  }
}
