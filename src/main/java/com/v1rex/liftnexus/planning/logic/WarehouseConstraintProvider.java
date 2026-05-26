package com.v1rex.liftnexus.planning.logic;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class WarehouseConstraintProvider implements ConstraintProvider {

  @Override
  public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
    return new Constraint[] {
      /*      forkliftCapacity(constraintFactory)
      minimizeTravelDistance(constraintFactory),
      taskEquipmentRequirement(constraintFactory)*/

    };
  }
  /*

    // Hard constraint: check if all the assigned transportOrders to a Forklift does
    // not exceed the capacity of the forklift
    private Constraint forkliftCapacity(ConstraintFactory factory) {
      return factory
          .forEach(TransportOrder.class)
          .filter(transportorder -> transportorder.getAssignedForklift() != null)
          .filter(transportorder ->
                  transportorder.getTargetLoadUnit().getWeightKg() >
                  transportorder.getAssignedForklift().getForkliftType().getMaxCapacityKg())
          .penalize(HardSoftScore.ONE_HARD)
          .asConstraint("Forklift capacity limit");
    }

    // Hard constraint: check if the assigned transportOrders to a Forklift is
    // compatible with the requirement equipment type of the transportorder
    private Constraint taskEquipmentRequirement(ConstraintFactory factory) {
      return factory
          .forEach(TransportOrder.class)
          .filter(transportorder -> transportorder.getForklift() != null)
          .filter(transportorder -> transportorder.getRequiredEquipment() != transportorder.getForklift().getEquipmentType())
          .penalize(HardSoftScore.ONE_HARD)
          .asConstraint("TransportOrder equipment type requirement");
    }

    // Soft constraint: sum complete travel distance of the forklift
    private Constraint minimizeTravelDistance(ConstraintFactory factory) {
      return factory
          .forEach(Forklift.class)
          .filter(forklift -> !forklift.getTransportOrders().isEmpty())
          .penalize(
              HardSoftScore.ONE_SOFT,
              forklift -> {
                int totalTraveledDistance = 0;

                List<TransportOrder> transportOrders = forklift.getTransportOrders();
                // initial drive to the first transportorder
                totalTraveledDistance +=
                    (int) forklift.getCurrentStorageBin().distanceTo(transportOrders.get(0).getPickStorageBin());

                for (int i = 0; i < transportOrders.size(); i++) {
                  TransportOrder current = transportOrders.get(i);

                  // We calculate the travel distance from currentTask
                  // to the Delivery StorageBin
                  totalTraveledDistance +=
                      (int) current.getPickStorageBin().distanceTo(current.getDeliveryStorageBin());

                  // if there is a next transportorder, we calculate the travel distance
                  // from the delivery storagebin to the pick storagebin
                  // of the next transportorder
                  if (i < transportOrders.size() - 1) {
                    TransportOrder next = transportOrders.get(i + 1);
                    totalTraveledDistance +=
                        (int) current.getDeliveryStorageBin().distanceTo(next.getPickStorageBin());
                  }
                }
                // todo: think about the metrics!!
                return totalTraveledDistance;
              })
          .asConstraint("Minimize travel distance");
    }
  */

}
