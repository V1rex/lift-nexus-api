package com.v1rex.liftnexus.planning.constraints;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class WarehouseConstraintProvider implements ConstraintProvider {

  @Override
  public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
      return new Constraint[] {
            ForkliftCapacityConstraint
                    .forkliftCapacity(constraintFactory),
            ForkliftTravelDistanceConstraint
                    .forkliftTravelDistance(constraintFactory),
            TransportOrderEquipmentRequirementConstraint
                    .equipmentType(constraintFactory)
      };
  }

}
