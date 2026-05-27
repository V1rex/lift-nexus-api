package com.v1rex.liftnexus.planning.constraints;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;

public class ForkliftTravelDistanceConstraint {

  // TODO: [PERFORMANCE OPTIMIZATION - MILESTONE 2 PREPARATION]
  // This O(N) loop on Forklift.class works perfectly for the MVP's small datasets.
  // However, it triggers a full chain recalculation on every micro-move
  public static Constraint forkliftTravelDistance(ConstraintFactory factory) {
    return factory
        .forEach(Forklift.class)
        .filter(
            forklift ->
                forklift.getTransportOrders() != null && !forklift.getTransportOrders().isEmpty())
        .penalize(
            HardSoftScore.ONE_SOFT, ForkliftTravelDistanceConstraint::calculateTotalTravelDistance)
        .asConstraint("Forklift travel distance");
  }

  private static int calculateTotalTravelDistance(Forklift forklift) {
    int totalTraveledDistance = 0;

    Coordinate3D currentLocationForklift = forklift.getCurrentStorageBin().getCoordinate();

    for (TransportOrder order : forklift.getTransportOrders()) {
      Coordinate3D sourceLocation = order.getSourceBin().getCoordinate();
      Coordinate3D targetLocation = order.getTargetBin().getCoordinate();

      totalTraveledDistance += (int) currentLocationForklift.calculateDistance(sourceLocation);

      totalTraveledDistance += (int) sourceLocation.calculateDistance(targetLocation);

      currentLocationForklift = targetLocation;
    }
    return totalTraveledDistance;
  }
}
