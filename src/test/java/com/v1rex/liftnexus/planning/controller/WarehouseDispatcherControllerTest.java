package com.v1rex.liftnexus.planning.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.v1rex.liftnexus.common.exception.GlobalExceptionHandler;
import com.v1rex.liftnexus.common.exception.ProblemDetailFactory;
import com.v1rex.liftnexus.planning.domain.JobStatus;
import com.v1rex.liftnexus.planning.dto.DispatchJobResponse;
import com.v1rex.liftnexus.planning.service.WarehouseDispatcherService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WarehouseDispatcherController.class)
@Import({
  GlobalExceptionHandler.class,
  DispatchJobExceptionHandler.class,
  ProblemDetailFactory.class
})
@DisplayName("WarehouseDispatcherController API Tests")
class WarehouseDispatcherControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private WarehouseDispatcherService dispatcherService;

  @Test
  @DisplayName("POST /api/v1/dispatcher/jobs should return 202 Accepted with Job ID")
  void shouldSubmitJobAndReturnAccepted() throws Exception {
    UUID mockJobId = UUID.randomUUID();
    when(dispatcherService.submitOptimizationJob()).thenReturn(mockJobId);

    mockMvc
        .perform(post("/api/v1/dispatcher/jobs"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.jobId").value(mockJobId.toString()));
  }

  @Test
  @DisplayName("GET /api/v1/dispatcher/jobs/{jobId} should return 200 OK with job details")
  void shouldGetJobStatusAndReturnOk() throws Exception {
    UUID mockJobId = UUID.randomUUID();
    DispatchJobResponse mockResponse =
        new DispatchJobResponse(mockJobId, JobStatus.SOLVING, Instant.now(), null, null);

    when(dispatcherService.getJobStatusAndReconcile(mockJobId)).thenReturn(mockResponse);

    mockMvc
        .perform(get("/api/v1/dispatcher/jobs/{jobId}", mockJobId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(mockJobId.toString()))
        .andExpect(jsonPath("$.status").value("SOLVING"));
  }

  @Test
  @DisplayName("DELETE /api/v1/dispatcher/jobs/{jobId} should return 204 No Content")
  void shouldTerminateJobAndReturnNoContent() throws Exception {
    UUID mockJobId = UUID.randomUUID();
    doNothing().when(dispatcherService).terminateOptimizationJob(mockJobId);

    mockMvc
        .perform(delete("/api/v1/dispatcher/jobs/{jobId}", mockJobId))
        .andExpect(status().isNoContent());
  }
}
