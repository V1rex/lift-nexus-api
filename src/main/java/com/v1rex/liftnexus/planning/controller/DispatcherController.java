package com.v1rex.liftnexus.planning.controller;

import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.planning.service.WarehouseDispatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dispatcher")
@RequiredArgsConstructor
public class DispatcherController {

    private final WarehouseDispatcherService dispatcherService;

    @PostMapping("/solve")
    public String solve() {
        dispatcherService.startSolving();
        return "Solver started in the background. Optimization is running.";
    }

    @GetMapping("/solution")
    public WarehouseSchedule getSolution() {
        return dispatcherService.buildCurrentState();
    }
}