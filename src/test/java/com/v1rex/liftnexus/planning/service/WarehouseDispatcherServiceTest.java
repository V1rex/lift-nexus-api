package com.v1rex.liftnexus.planning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
import com.v1rex.liftnexus.planning.DispatchJobRepository;
import com.v1rex.liftnexus.planning.domain.DispatchJob;
import com.v1rex.liftnexus.planning.domain.JobStatus;
import com.v1rex.liftnexus.planning.domain.WarehouseSchedule;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import com.v1rex.liftnexus.transportorder.service.TransportOrderService;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

  @Mock private DispatchJobRepository jobRepository;

  @Mock private SolverManager<WarehouseSchedule> solverManager;

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

  @Nested
  @DisplayName("Feature: Submit Optimization Job")
  class SubmitOptimizationJob {

    @Test
    void shouldCreateTicketInQueuedStatusAndStartSolver() {
      ArgumentCaptor<DispatchJob> jobCaptor = ArgumentCaptor.forClass(DispatchJob.class);

      when(jobRepository.save(any(DispatchJob.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      when(jobRepository.findById(any(UUID.class)))
          .thenAnswer(
              invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(
                    DispatchJob.builder()
                        .id(id)
                        .status(JobStatus.QUEUED)
                        .createdAt(Instant.now())
                        .build());
              });

      when(storageBinService.findAllEntities()).thenReturn(Collections.emptyList());
      when(forkliftService.findAllEntities()).thenReturn(Collections.emptyList());
      when(transportOrderService.findAllEntities()).thenReturn(Collections.emptyList());

      UUID returnedTicketId = warehouseDispatcherService.submitOptimizationJob();

      verify(jobRepository, times(2)).save(jobCaptor.capture());

      DispatchJob initialSavedJob = jobCaptor.getAllValues().get(0);
      assertThat(returnedTicketId).isNotNull();
      assertThat(initialSavedJob.getId()).isEqualTo(returnedTicketId);
      assertThat(initialSavedJob.getStatus()).isEqualTo(JobStatus.QUEUED);

      verify(solverManager, times(1))
          .solveAndListen(eq(returnedTicketId), any(WarehouseSchedule.class), any());
    }
  }

  @Nested
  @DisplayName("Feature: Background Worker Initialization")
  class BackgroundWorkerInitialization {

    @Test
    void shouldTransitionJobToSolvingAndReturnCurrentProblemState() {

      UUID targetJobId = UUID.randomUUID();
      DispatchJob existingQueuedJob =
          DispatchJob.builder()
              .id(targetJobId)
              .status(JobStatus.QUEUED)
              .createdAt(Instant.now())
              .build();

      when(jobRepository.findById(targetJobId)).thenReturn(Optional.of(existingQueuedJob));
      when(jobRepository.save(any(DispatchJob.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      when(storageBinService.findAllEntities()).thenReturn(Collections.emptyList());
      when(forkliftService.findAllEntities()).thenReturn(Collections.emptyList());
      when(transportOrderService.findAllEntities()).thenReturn(Collections.emptyList());

      WarehouseSchedule resultProblemState =
          warehouseDispatcherService.buildCurrentProblemAndSetSolvingStatus(targetJobId);

      ArgumentCaptor<DispatchJob> updatedJobCaptor = ArgumentCaptor.forClass(DispatchJob.class);
      verify(jobRepository, times(1)).save(updatedJobCaptor.capture());

      assertThat(updatedJobCaptor.getValue().getStatus()).isEqualTo(JobStatus.SOLVING);

      assertThat(resultProblemState).isNotNull();
      assertThat(resultProblemState.getStorageBins()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenJobTicketDoesNotExistInDatabase() {
      // Given
      UUID nonExistentJobId = UUID.randomUUID();
      when(jobRepository.findById(nonExistentJobId)).thenReturn(Optional.empty());

      // When / Then
      assertThatThrownBy(
              () ->
                  warehouseDispatcherService.buildCurrentProblemAndSetSolvingStatus(
                      nonExistentJobId))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Job not found: " + nonExistentJobId);

      verify(jobRepository, never()).save(any());
    }
  }
}
