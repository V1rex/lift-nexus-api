package com.v1rex.liftnexus.planning.constraints;

import static org.assertj.core.api.Assertions.assertThat;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WarehouseConstraintProvider Integration Tests")
class WarehouseConstraintProviderTest {

  private final ConstraintVerifier<WarehouseConstraintProvider, WarehouseSchedule>
      constraintVerifier =
          ConstraintVerifier.build(
              new WarehouseConstraintProvider(),
              WarehouseSchedule.class,
              TransportOrder.class,
              Forklift.class);

  @Test
  @DisplayName("All constraints should be registered and return zero penalty for an empty schedule")
  void shouldRegisterAllConstraintsCleanly() {

    assertThat(constraintVerifier).isNotNull();

    constraintVerifier
        .verifyThat(WarehouseConstraintProvider::forkliftCapacity)
        .given()
        .penalizesBy(0);

    constraintVerifier
        .verifyThat(WarehouseConstraintProvider::travelDistance)
        .given()
        .penalizesBy(0);

    constraintVerifier
        .verifyThat(WarehouseConstraintProvider::equipmentType)
        .given()
        .penalizesBy(0);
  }
}
