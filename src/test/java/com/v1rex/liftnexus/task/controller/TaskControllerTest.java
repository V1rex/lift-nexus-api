package com.v1rex.liftnexus.task.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.task.dto.TaskRequest;
import com.v1rex.liftnexus.task.dto.TaskResponse;
import com.v1rex.liftnexus.task.dto.TaskStatusUpdateRequest;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import com.v1rex.liftnexus.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@ActiveProfiles("test")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();


    @MockitoBean
    private TaskService taskService;

    @Nested
    @DisplayName("Tests - GET /api/v1/tasks/{id}")
    class GetTaskById{

        @Test
        @DisplayName("Should return a Task when GET request is made to /api/v1/tasks/{id}")
        void shouldReturnTask_WhenGetRequestIsMadeToFindById() throws Exception {
            TaskResponse mockDto = new TaskResponse(10L,
                    null,
                    null,
                    15,
                    EquipmentType.STANDARD,
                    TaskStatus.OPEN,
                    5L);
            when(taskService.findById(10L)).thenReturn(mockDto);

             mockMvc.perform(get("/api/v1/tasks/10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.weight").value(15))
                    .andExpect(jsonPath("$.requiredEquipment").value("STANDARD"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.forkliftId").value(5));
        }

        @Test
        @DisplayName("Should return 404 Not Found when entity missing")
        void shouldReturnNotFound_WhenGetRequestIsMadeToFindByIdWithNonExistingId() throws Exception {
            Long nonExistingId = 1L;

            when(taskService.findById(nonExistingId))
                    .thenThrow(new ResourceNotFoundException("Task with " + nonExistingId + " not found."));

            mockMvc.perform(get("/api/v1/tasks/{id}", nonExistingId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }


    }

    @Nested
    @DisplayName("Tests - GET /api/v1/tasks/search")
    class SearchTasks {
        @Test
        @DisplayName("Should return 200 OK with a paginated list of tasks when search parameters are valid")
        void shouldReturnPagedTasks_WhenSearchParamsAreValid() throws Exception {
            TaskResponse responseDto = new TaskResponse(10L, null, null, 500, EquipmentType.STANDARD, TaskStatus.OPEN, null);
            Page<TaskResponse> mockPage = new PageImpl<>(java.util.List.of(responseDto));

            when(taskService.searchTasks(eq(TaskStatus.OPEN),
                    eq(500),
                    any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(get("/api/v1/tasks/search")
                            .param("status", "OPEN")
                            .param("minWeight", "500")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(10))
                    .andExpect(jsonPath("$.content[0].weight").value(500))
                    .andExpect(jsonPath("$.content[0].status").value("OPEN"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when minWeight parameter violates @Min(1)")
        void shouldReturnBadRequest_WhenMinWeightIsLessThanOne() throws Exception {
            // Act & Assert (Fails before hitting service layer due to @Min(1))
            mockMvc.perform(get("/api/v1/tasks/search")
                            .param("minWeight", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests - POST /api/v1/tasks")
    class CreateTask {
        @Test
        @DisplayName("Should return 201 Created along with valid Location header when data request body is valid")
        void shouldReturnCreatedTaskWithLocationHeader_WhenRequestBodyIsValid() throws Exception {
            TaskRequest mockRequest = new TaskRequest(
                    1L,
                    2L,
                    TaskStatus.OPEN,
                    EquipmentType.STANDARD,
                    750
            );

            TaskResponse mockResponse = new TaskResponse(
                    42L,
                    null,
                    null,
                    750,
                    EquipmentType.STANDARD,
                    TaskStatus.OPEN,
                    null
            );

            when(taskService.createTask(any(TaskRequest.class)))
                    .thenReturn(mockResponse);


            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(mockRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                           containsString("/api/v1/tasks/42")))
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.weight").value(750))
                    .andExpect(jsonPath("$.status").value("OPEN"));
        }
    }

    @Nested
    @DisplayName("Tests- PUT /api/v1/tasks")
    class UpdateTask {

        @Test
        @DisplayName("Should return 200 OK with updated task details when status adjustment is valid")
        void shouldReturnUpdatedTask_WhenUpdatePayloadIsSuccessful() throws Exception {

            TaskStatusUpdateRequest updateRequest = new TaskStatusUpdateRequest(TaskStatus.COMPLETED);


            TaskResponse mockResponse = new TaskResponse(
                    10L,
                    null,
                    null,
                    500,
                    EquipmentType.STANDARD,
                    TaskStatus.COMPLETED,
                    5L
            );

            when(taskService.updateTask(eq(10L), any(TaskStatusUpdateRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(put("/api/v1/tasks/{id}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }
    }


}
