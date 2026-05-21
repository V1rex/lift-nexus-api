package com.v1rex.liftnexus.planning.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.task.domain.Task;
import java.util.List;
import lombok.*;

@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseSchedule {

  @ProblemFactCollectionProperty private List<Location> locations;

  @ValueRangeProvider(id = "taskPoolRange")
  @PlanningEntityCollectionProperty
  private List<Task> taskPool;

  @PlanningEntityCollectionProperty private List<Forklift> forklifts;

  @PlanningScore private HardSoftScore score;
}
