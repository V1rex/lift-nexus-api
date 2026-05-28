package com.v1rex.liftnexus.planning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.v1rex.liftnexus.forklift.service.ForkliftService;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.transportorder.service.TransportOrderService;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseDispatcherService Logic Tests")
@ActiveProfiles("test")
public class WarehouseDispatcherServiceTest {

  @Mock private StorageBinService storageBinService;

  @Mock private ForkliftService forkliftService;

  @Mock private TransportOrderService transportOrderService;

  @InjectMocks private WarehouseDispatcherService warehouseDispatcherService;

  @Nested
  @DisplayName("Feature: Build Current State for Optimization")
  class BuildCurrentState {

    @Test
    void shouldReturnCorrectLoadedState() {
      when(storageBinService.findAllEntities()).thenReturn(Collections.emptyList());
      when(forkliftService.findAllEntities()).thenReturn(Collections.emptyList());
      when(transportOrderService.findAllEntities()).thenReturn(Collections.emptyList());

      WarehouseSchedule schedule = warehouseDispatcherService.buildCurrentState();

      assertThat(schedule).isNotNull();
      assertThat(schedule.getStorageBins()).isEmpty();
      assertThat(schedule.getForklifts()).isEmpty();
      assertThat(schedule.getTransportOrderPool()).isEmpty();
    }
  }
}
