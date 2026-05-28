package com.v1rex.liftnexus.planning.constraints;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class WarehouseConstraintProvider implements ConstraintProvider {

  @Override
  public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
    return new Constraint[] {
      forkliftCapacity(constraintFactory),
      travelDistance(constraintFactory),
      equipmentType(constraintFactory)
    };
  }

  public Constraint forkliftCapacity(ConstraintFactory factory) {
    return ForkliftCapacityConstraint.forkliftCapacity(factory);
  }

  public Constraint travelDistance(ConstraintFactory factory) {
    return ForkliftTravelDistanceConstraint.forkliftTravelDistance(factory);
  }

  public Constraint equipmentType(ConstraintFactory factory) {
    return TransportOrderEquipmentRequirementConstraint.equipmentType(factory);
  }
}
