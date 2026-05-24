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
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderRequest;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderStatusUpdateRequest;
import com.v1rex.liftnexus.transportorder.service.TransportOrderService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransportOrderController.class)
@DisplayName("TransportOrderController Gateway Tests")
class TransportOrderControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private TransportOrderService transportOrderService;

  @Nested
  @DisplayName("Tests - GET /api/v1/transport-orders/{id}")
  class GetOrderById {

    @Test
    @DisplayName("Should return 200 OK with details when order exists")
    void shouldReturnOrder_WhenIdExists() throws Exception {
      TransportOrderResponse response =
          new TransportOrderResponse(
              1L, "SKU-999", 10L, 2L, 1L, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      when(transportOrderService.findById(1L)).thenReturn(response);

      mockMvc
          .perform(get("/api/v1/transport-orders/{id}", 1L))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.trackingCode").value("SKU-999"))
          .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when ID does not exist in system")
    void shouldReturn404_WhenIdDoesNotExist() throws Exception {
      when(transportOrderService.findById(99L))
          .thenThrow(new ResourceNotFoundException("TransportOrder with ID 99 not found."));

      mockMvc
          .perform(get("/api/v1/transport-orders/{id}", 99L))
          .andDo(print())
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/transport-orders/search")
  class SearchOrders {

    @Test
    @DisplayName("Should return 200 OK with a page of matching records when parameters are valid")
    void shouldReturnPagedOrders_WhenCriteriaAreValid() throws Exception {
      TransportOrderResponse response =
          new TransportOrderResponse(
              1L, "SKU-999", 10L, 2L, 1L, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      Page<TransportOrderResponse> page = new PageImpl<>(List.of(response));

      when(transportOrderService.searchOrders(
              eq(TransportOrderStatus.OPEN), eq(500), any(Pageable.class)))
          .thenReturn(page);

      mockMvc
          .perform(
              get("/api/v1/transport-orders/search")
                  .param("status", "OPEN")
                  .param("minWeight", "500")
                  .param("page", "0")
                  .param("size", "20"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(1))
          .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName(
        "Should return 400 Bad Request when requested minWeight is below zero constraint limit")
    void shouldReturn400_WhenMinWeightIsLessThanOne() throws Exception {
      mockMvc
          .perform(get("/api/v1/transport-orders/search").param("minWeight", "0"))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Tests - POST /api/v1/transport-orders")
  class CreateTransportOrder {

    @Test
    @DisplayName("Should return 201 Created with Location header when payload is valid")
    void shouldCreateAndReturn201() throws Exception {
      TransportOrderRequest request =
          new TransportOrderRequest(10L, 1L, 2L, EquipmentType.STANDARD);

      TransportOrderResponse response =
          new TransportOrderResponse(
              42L, "SKU-999", 10L, 2L, 1L, EquipmentType.STANDARD, TransportOrderStatus.OPEN, null);

      when(transportOrderService.createTransportOrder(any(TransportOrderRequest.class)))
          .thenReturn(response);

      mockMvc
          .perform(
              post("/api/v1/transport-orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", containsString("/api/v1/transport-orders/42")))
          .andExpect(jsonPath("$.id").value(42))
          .andExpect(jsonPath("$.trackingCode").value("SKU-999"))
          .andExpect(jsonPath("$.targetLoadUnitId").value(10))
          .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName(
        "Should return 400 Bad Request when mandatory layout variables are completely null")
    void shouldReturn400_WhenPayloadAttributesAreMissing() throws Exception {
      TransportOrderRequest invalidRequest = new TransportOrderRequest(null, null, null, null);

      mockMvc
          .perform(
              post("/api/v1/transport-orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Tests - PUT /api/v1/transport-orders/{id}/status")
  class UpdateTransportOrder {

    @Test
    @DisplayName(
        "Should return 200 OK with updated transportorder details when status adjustment is valid")
    void shouldReturnUpdatedTask_WhenUpdatePayloadIsSuccessful() throws Exception {
      TransportOrderStatusUpdateRequest updateRequest =
          new TransportOrderStatusUpdateRequest(TransportOrderStatus.COMPLETED);

      TransportOrderResponse mockResponse =
          new TransportOrderResponse(
              10L,
              "SKU-999",
              10L,
              2L,
              1L,
              EquipmentType.STANDARD,
              TransportOrderStatus.COMPLETED,
              null);

      when(transportOrderService.updateOrderStatus(
              eq(10L), any(TransportOrderStatusUpdateRequest.class)))
          .thenReturn(mockResponse);

      mockMvc
          .perform(
              put("/api/v1/transport-orders/{id}/status", 10L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when targeted state updates are null")
    void shouldReturn400_WhenTargetStatusIsNull() throws Exception {
      TransportOrderStatusUpdateRequest invalidRequest =
          new TransportOrderStatusUpdateRequest(null);

      mockMvc
          .perform(
              put("/api/v1/transport-orders/{id}/status", 10L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }
}
