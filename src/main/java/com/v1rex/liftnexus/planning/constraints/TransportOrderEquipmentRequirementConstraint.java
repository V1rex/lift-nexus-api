package com.v1rex.liftnexus.planning.constraints;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;

public class TransportOrderEquipmentRequirementConstraint {

  public static Constraint equipmentType(ConstraintFactory factory) {
    return factory
        .forEach(TransportOrder.class)
        .filter(order -> order.getAssignedForklift() != null)
        .filter(order -> order.getRequiredEquipment() != null)
        .filter(
            order ->
                !order
                    .getRequiredEquipment()
                    .equals(order.getAssignedForklift().getForkliftType().getEquipmentType()))
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Transport order equipment requirement");
  }
}
