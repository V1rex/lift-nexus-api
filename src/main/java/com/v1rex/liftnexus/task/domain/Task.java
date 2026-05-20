package com.v1rex.liftnexus.task.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import com.v1rex.liftnexus.location.domain.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@PlanningEntity
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pick_location_id", nullable = false)
    private Location pickLocation;

    @ManyToOne
    @JoinColumn(name = "delivery_location_id", nullable = false)
    private Location deliveryLocation;


    @NotNull @Min(value = 1, message = "Weight must be greater than 0")
    @Column(name = "weight", nullable = false, columnDefinition = "integer check (weight > 0)")
    private Integer weight;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.OPEN;


    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "required_equipment", nullable = false)
    private EquipmentType requiredEquipment = EquipmentType.STANDARD;


    @InverseRelationShadowVariable(sourceVariableName = "tasks")
    @ManyToOne
    @JoinColumn(name = "forklift_id")
    @JsonIgnore
    private Forklift forklift;

    @PlanningPin
    public boolean isPinned() {
        return status == TaskStatus.IN_PROGRESS || status == TaskStatus.COMPLETED;
    }
}