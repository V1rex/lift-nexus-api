/*
package com.v1rex.liftnexus.transportorder.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.service.TransportOrderService;
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

@WebMvcTest(controllers = TransportOrderController.class)
@ActiveProfiles("test")
public class TaskControllerTest {

  @Autowired private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private TransportOrderService taskService;

  @Nested
  @DisplayName("Tests - GET /api/v1/transportOrders/{id}")
  class GetTaskById {

    @Test
    @DisplayName("Should return a TransportOrder when GET request is made to /api/v1/transportOrders/{id}")
    void shouldReturnTask_WhenGetRequestIsMadeToFindById() throws Exception {
      TransportOrderResponse mockDto =
          new TransportOrderResponse(10L, null, null, 15, EquipmentType.STANDARD, TransportOrderStatus.OPEN, 5L);
      when(taskService.findById(10L)).thenReturn(mockDto);

      mockMvc
          .perform(get("/api/v1/transportOrders/10"))
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
          .thenThrow(new ResourceNotFoundException("TransportOrder with " + nonExistingId + " not found."));

      mockMvc
          .perform(get("/api/v1/transportOrders/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/transportOrders/search")
  class SearchTasks {
    @Test
    @DisplayName(
        "Should return 200 OK with a paginated list of transportOrders when search parameters are valid")
    void shouldReturnPagedTasks_WhenSearchParamsAreValid() throws Exception {
      TransportOrderResponse responseDto =
          new TransportOrderResponse(10L, null, null, 500, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);
      Page<TransportOrderResponse> mockPage = new PageImpl<>(java.util.List.of(responseDto));

      when(taskService.searchTasks(
              eq(TransportOrderStatus.OPEN), eq(500), any(org.springframework.data.domain.Pageable.class)))
          .thenReturn(mockPage);

      mockMvc
          .perform(
              get("/api/v1/transportOrders/search")
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
      mockMvc
          .perform(
              get("/api/v1/transportOrders/search")
                  .param("minWeight", "0")
                  .accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Tests - POST /api/v1/transportOrders")
  class CreateTask {
    @Test
    @DisplayName(
        "Should return 201 Created along with valid StorageBin header when data request body is valid")
    void shouldReturnCreatedTaskWithLocationHeader_WhenRequestBodyIsValid() throws Exception {
      TransportOrderRequest mockRequest =
          new TransportOrderRequest(1L, 2L, TransportOrderStatus.OPEN, EquipmentType.STANDARD, 750);

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(42L, null, null, 750, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      when(taskService.createTask(any(TransportOrderRequest.class))).thenReturn(mockResponse);

      mockMvc
          .perform(
              post("/api/v1/transportOrders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(mockRequest)))
          .andDo(print())
          .andExpect(status().isCreated())
          .andExpect(header().string("StorageBin", containsString("/api/v1/transportOrders/42")))
          .andExpect(jsonPath("$.id").value(42))
          .andExpect(jsonPath("$.weight").value(750))
          .andExpect(jsonPath("$.status").value("OPEN"));
    }
  }

  @Nested
  @DisplayName("Tests- PUT /api/v1/transportOrders")
  class UpdateTask {

    @Test
    @DisplayName("Should return 200 OK with updated transportorder details when status adjustment is valid")
    void shouldReturnUpdatedTask_WhenUpdatePayloadIsSuccessful() throws Exception {

      TransportOrderStatusUpdateRequest updateRequest = new TransportOrderStatusUpdateRequest(TransportOrderStatus.COMPLETED);

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(10L, null, null, 500, EquipmentType.STANDARD, TransportOrderStatus.COMPLETED, 5L);

      when(taskService.updateTask(eq(10L), any(TransportOrderStatusUpdateRequest.class)))
          .thenReturn(mockResponse);

      mockMvc
          .perform(
              put("/api/v1/transportOrders/{id}", 10L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(10))
          .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
  }
}
*/
