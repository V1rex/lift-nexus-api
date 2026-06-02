package com.v1rex.liftnexus.config;

import ai.timefold.solver.core.config.solver.SolverConfig;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.planning.constraints.WarehouseConstraintProvider;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class TimefoldTestConfig {

  @Bean
  SolverConfig solverConfig() {
    return new SolverConfig()
        .withSolutionClass(WarehouseSchedule.class)
        .withEntityClasses(Forklift.class, TransportOrder.class)
        .withConstraintProviderClass(WarehouseConstraintProvider.class);
  }
}
