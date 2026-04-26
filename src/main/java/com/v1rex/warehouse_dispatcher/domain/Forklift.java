package com.v1rex.warehouse_dispatcher.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@PlanningEntity
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Forklift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(value = 1, message = "Weight must be greater than 0")
    @Column(name = "weight_capacity", nullable = false, columnDefinition = "integer check (weight_capacity >0)")
    private Integer weightCapacity;

    @PlanningListVariable(valueRangeProviderRefs = "taskPoolRange")
    @OneToMany(mappedBy = "forklift",
                cascade = CascadeType.ALL,
                fetch = FetchType.EAGER)
    private List<Task> tasks = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;

}