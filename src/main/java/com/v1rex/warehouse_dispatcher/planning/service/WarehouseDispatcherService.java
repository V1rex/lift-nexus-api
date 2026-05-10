package com.v1rex.warehouse_dispatcher.planning.service;


import ai.timefold.solver.core.api.solver.SolverManager;
import com.v1rex.warehouse_dispatcher.forklift.domain.Forklift;
import com.v1rex.warehouse_dispatcher.location.domain.Location;
import com.v1rex.warehouse_dispatcher.task.domain.Task;
import com.v1rex.warehouse_dispatcher.planning.domain.WarehouseSchedule;
import com.v1rex.warehouse_dispatcher.forklift.repository.ForkliftRepository;
import com.v1rex.warehouse_dispatcher.location.repository.LocationRepository;
import com.v1rex.warehouse_dispatcher.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseDispatcherService {
    private final LocationRepository locationRepository;
    private final ForkliftRepository forkliftRepository;
    private final TaskRepository taskRepository;

    private final SolverManager<WarehouseSchedule> solverManager;

    private WarehouseSchedule bestSolution;

    public WarehouseSchedule buildCurrentState() {
        // 1. Fetch data from the database
        List<Location> locations = locationRepository.findAll();
        List<Forklift> forklifts = forkliftRepository.findAll();
        List<Task> tasks = taskRepository.findAll();

        // 2. Assemble the "Whiteboard" (The Planning Solution)
        WarehouseSchedule schedule = new WarehouseSchedule();
        schedule.setLocations(locations);
        schedule.setForklifts(forklifts);
        schedule.setTaskPool(tasks);

        // 3. Return the fully loaded state ready for optimization
        return schedule;
    }

    public void startSolving() {
        WarehouseSchedule problem = buildCurrentState();
        // Update the bestSolution as the solver finds better ones
        // Explicitly define the ID and the lambda
        Long problemId = 1L;
        solverManager.solveAndListen(problemId,
                problem,
                this::saveSolution);
    }

    public WarehouseSchedule getSolution() {
        return bestSolution != null ? bestSolution : buildCurrentState();
    }

    @Transactional
    public void saveSolution(WarehouseSchedule solution) {
        for (Forklift forklift : solution.getForklifts()) {
            for (Task task : forklift.getTasks()) {
                // MANUALLY sync the relationship before saving
                task.setForklift(forklift);
                taskRepository.save(task);
            }
            forkliftRepository.save(forklift);
        }
    }
}
