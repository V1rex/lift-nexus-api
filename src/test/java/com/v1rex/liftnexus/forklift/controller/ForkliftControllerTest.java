package com.v1rex.liftnexus.forklift.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.OperationalStatus;
import com.v1rex.liftnexus.forklift.dto.ForkliftLocationUpdateRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
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

@WebMvcTest(ForkliftController.class)
public class ForkliftControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private ForkliftService forkliftService;

  private ForkliftResponse createMockResponse() {
    return new ForkliftResponse(
        1L,
        "FL-01",
        10L,
        "Toyota X",
        EquipmentType.STANDARD,
        2000,
        5L,
        OperationalStatus.ACTIVE,
        100.0,
        List.of());
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/forklifts/{id}")
  class GetForkliftByIdTest {

    @Test
    @DisplayName("Should return 200 OK with requested forklift")
    void shouldReturnById() throws Exception {
      ForkliftResponse response = createMockResponse();

      when(forkliftService.findById(1L)).thenReturn(response);

      mockMvc
          .perform(get("/api/v1/forklifts/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.fleetNumber").value("FL-01"));
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/forklifts")
  class GetAllForkliftsTest {

    @Test
    @DisplayName("Should return 200 OK with paginated list of all forklifts")
    void shouldReturnPaginatedList() throws Exception {
      Page<ForkliftResponse> page = new PageImpl<>(List.of(createMockResponse()));

      when(forkliftService.findAll(any(Pageable.class))).thenReturn(page);

      mockMvc
          .perform(get("/api/v1/forklifts"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(1))
          .andExpect(jsonPath("$.content[0].fleetNumber").value("FL-01"));
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/forklifts/search")
  class SearchForkliftsTest {

    @Test
    @DisplayName("Branch 1: Should search by minCapacity and return 200 OK")
    void shouldSearchByMinCapacity() throws Exception {
      Page<ForkliftResponse> page = new PageImpl<>(List.of(createMockResponse()));

      when(forkliftService.findWithCapacityGreaterThan(eq(2000), any(Pageable.class)))
          .thenReturn(page);

      mockMvc
          .perform(get("/api/v1/forklifts/search").param("minCapacity", "2000"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].maxCapacityKg").value(2000));
    }

    @Test
    @DisplayName("Branch 2: Should search by operational status and return 200 OK")
    void shouldSearchByStatus() throws Exception {
      Page<ForkliftResponse> page = new PageImpl<>(List.of(createMockResponse()));

      when(forkliftService.findByStatus(eq(OperationalStatus.ACTIVE), any(Pageable.class)))
          .thenReturn(page);

      mockMvc
          .perform(get("/api/v1/forklifts/search").param("status", "ACTIVE"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Branch 3: Should fallback to findAll if no search params provided")
    void shouldFallbackToFindAll() throws Exception {
      Page<ForkliftResponse> page = new PageImpl<>(List.of(createMockResponse()));

      when(forkliftService.findAll(any(Pageable.class))).thenReturn(page);

      mockMvc
          .perform(get("/api/v1/forklifts/search"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(1));
    }
  }

  @Nested
  @DisplayName("Tests - POST /api/v1/forklifts")
  class CreateForkliftTest {

    @Test
    @DisplayName("Should create forklift, return 201 Created and Location header")
    void shouldCreateAndReturn201() throws Exception {
      ForkliftRequest request =
          new ForkliftRequest("FL-01", 10L, 5L, OperationalStatus.ACTIVE, 100.0);
      ForkliftResponse response = createMockResponse();

      when(forkliftService.createForklift(any(ForkliftRequest.class))).thenReturn(response);

      mockMvc
          .perform(
              post("/api/v1/forklifts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "http://localhost/api/v1/forklifts/1"))
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.fleetNumber").value("FL-01"));
    }
  }

  @Nested
  @DisplayName("Tests - PUT /api/v1/forklifts/{id}/location")
  class UpdateForkliftLocationTest {

    @Test
    @DisplayName("Should update forklift location and return 200 OK")
    void shouldUpdateLocation() throws Exception {
      ForkliftLocationUpdateRequest request = new ForkliftLocationUpdateRequest(99L);
      ForkliftResponse response = createMockResponse();

      when(forkliftService.updateForkliftLocation(eq(1L), eq(99L))).thenReturn(response);

      mockMvc
          .perform(
              put("/api/v1/forklifts/1/location")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1));
    }
  }

  @Nested
  @DisplayName("Tests - PATCH /api/v1/forklifts/{id}/status")
  class UpdateOperationalStatusTest {

    @Test
    @DisplayName("Should update operational status and return 200 OK")
    void shouldUpdateStatus() throws Exception {
      ForkliftResponse response = createMockResponse();

      when(forkliftService.updateOperationalStatus(eq(1L), eq(OperationalStatus.MAINTENANCE)))
          .thenReturn(response);

      mockMvc
          .perform(patch("/api/v1/forklifts/1/status").param("status", "MAINTENANCE"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1));
    }
  }
}
