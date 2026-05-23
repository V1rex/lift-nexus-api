/*
package com.v1rex.liftnexus.planning.logic;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.task.domain.Task;
import java.util.List;

public class WarehouseConstraintProvider implements ConstraintProvider {

  @Override
  public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
    return new Constraint[] {
      forkliftCapacity(constraintFactory),
      minimizeTravelDistance(constraintFactory),
      taskEquipmentRequirement(constraintFactory)
    };
  }

  // Hard constraint: check if all the assigned tasks to a Forklift does
  // not exceed the capacity of the forklift
  private Constraint forkliftCapacity(ConstraintFactory factory) {
    return factory
        .forEach(Task.class) // Start with the Task
        .filter(task -> task.getForklift() != null)
        .filter(task -> task.getWeight() > task.getForklift().getWeightCapacity())
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Forklift capacity limit");
  }

  // Hard constraint: check if the assigned tasks to a Forklift is
  // compatible with the requirement equipment type of the task
  private Constraint taskEquipmentRequirement(ConstraintFactory factory) {
    return factory
        .forEach(Task.class)
        .filter(task -> task.getForklift() != null)
        .filter(task -> task.getRequiredEquipment() != task.getForklift().getEquipmentType())
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Task equipment type requirement");
  }

  // Soft constraint: sum complete travel distance of the forklift
  private Constraint minimizeTravelDistance(ConstraintFactory factory) {
    return factory
        .forEach(Forklift.class)
        .filter(forklift -> !forklift.getTasks().isEmpty())
        .penalize(
            HardSoftScore.ONE_SOFT,
            forklift -> {
              int totalTraveledDistance = 0;

              List<Task> tasks = forklift.getTasks();
              // initial drive to the first task
              totalTraveledDistance +=
                  (int) forklift.getCurrentStorageBin().distanceTo(tasks.get(0).getPickStorageBin());

              for (int i = 0; i < tasks.size(); i++) {
                Task current = tasks.get(i);

                // We calculate the travel distance from currentTask
                // to the Delivery StorageBin
                totalTraveledDistance +=
                    (int) current.getPickStorageBin().distanceTo(current.getDeliveryStorageBin());

                // if there is a next task, we calculate the travel distance
                // from the delivery storagebin to the pick storagebin
                // of the next task
                if (i < tasks.size() - 1) {
                  Task next = tasks.get(i + 1);
                  totalTraveledDistance +=
                      (int) current.getDeliveryStorageBin().distanceTo(next.getPickStorageBin());
                }
              }
              // todo: think about the metrics!!
              return totalTraveledDistance;
            })
        .asConstraint("Minimize travel distance");
  }
}
*/
