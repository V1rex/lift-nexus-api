package com.v1rex.liftnexus.planning.constraints;


import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;

import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseConstraintProviderTest {

    private final ConstraintVerifier<WarehouseConstraintProvider, WarehouseSchedule> constraintVerifier =
            ConstraintVerifier.build(new WarehouseConstraintProvider(),
                    WarehouseSchedule.class,
                    TransportOrder.class,
                    Forklift.class);

    @Test
    void shouldRegisterAllConstraintsCleanly() {

        assertThat(constraintVerifier).isNotNull();


        constraintVerifier.verifyThat(WarehouseConstraintProvider::forkliftCapacity)
                .given()
                .penalizesBy(0);

        constraintVerifier
                .verifyThat(WarehouseConstraintProvider::travelDistance)
                .given()
                .penalizesBy(0);

        constraintVerifier.verifyThat(WarehouseConstraintProvider::equipmentType)
                .given()
                .penalizesBy(0);
    }
}