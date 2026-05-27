package com.v1rex.liftnexus.planning.constraints;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;

public class ForkliftCapacityConstraint {
  public static Constraint forkliftCapacity(ConstraintFactory factory) {
    return factory
        .forEach(TransportOrder.class)
        .filter(transportorder -> transportorder.getAssignedForklift() != null)
        .filter(
            transportorder ->
                transportorder.getTargetLoadUnit().getWeightKg()
                    > transportorder.getAssignedForklift().getForkliftType().getMaxCapacityKg())
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Forklift capacity limit");
  }
}
