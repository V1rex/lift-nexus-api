package com.v1rex.liftnexus.loadunit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitRequest;
import com.v1rex.liftnexus.loadunit.dto.LoadUnitResponse;
import com.v1rex.liftnexus.loadunit.service.LoadUnitService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoadUnitController.class)
@DisplayName("LoadUnitController Gateway Endpoint Tests")
class LoadUnitControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private LoadUnitService loadUnitService;

  @Nested
  @DisplayName("Endpoint: POST /api/v1/load-units")
  class CreateLoadUnitEndpoint {

    @Test
    @DisplayName(
        "Should accept correct JSON configurations and return 201 Created status with accurate context locations")
    void shouldCreateLoadUnitAndReturnCreated() throws Exception {
      LoadUnitRequest validRequest =
          new LoadUnitRequest("LU-CTRL-01", 620, LoadUnitStatus.STORED, 5L);
      LoadUnitResponse mockResponse =
          new LoadUnitResponse(42L, "LU-CTRL-01", 620, LoadUnitStatus.STORED, 5L, 1L);

      when(loadUnitService.createLoadUnit(any(LoadUnitRequest.class))).thenReturn(mockResponse);

      mockMvc
          .perform(
              post("/api/v1/load-units")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "http://localhost/api/v1/load-units/42"))
          .andExpect(jsonPath("$.id").value(42))
          .andExpect(jsonPath("$.trackingCode").value("LU-CTRL-01"))
          .andExpect(jsonPath("$.weightKg").value(620))
          .andExpect(jsonPath("$.currentStorageBinId").value(5));
    }

    @Test
    @DisplayName(
        "Should capture validation payload defects and decline request processing early with 400 Bad Request status")
    void shouldReturnBadRequestOnValidationFailure() throws Exception {
      // Defect: Empty tracking code, invalid negative mass weight parameter values
      LoadUnitRequest structuralDefectPayload =
          new LoadUnitRequest("", -50, LoadUnitStatus.EXPECTED, null);

      mockMvc
          .perform(
              post("/api/v1/load-units")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(structuralDefectPayload)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Endpoint: GET /api/v1/load-units/{id}")
  class GetLoadUnitEndpoint {

    @Test
    @DisplayName(
        "Should serialize response accurately with 200 OK status if database element match is confirmed")
    void shouldReturnLoadUnitWhenFound() throws Exception {
      LoadUnitResponse activeResponse =
          new LoadUnitResponse(12L, "LU-FOUND", 15, LoadUnitStatus.SHIPPED, null, 0L);
      when(loadUnitService.findById(12L)).thenReturn(activeResponse);

      mockMvc
          .perform(get("/api/v1/load-units/12"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.trackingCode").value("LU-FOUND"))
          .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @DisplayName(
        "Should transform infrastructure exception maps to standard 404 Not Found returns securely")
    void shouldReturnNotFoundOnMissingElement() throws Exception {
      when(loadUnitService.findById(404L))
          .thenThrow(new ResourceNotFoundException("Load Unit with ID 404 not found."));

      mockMvc.perform(get("/api/v1/load-units/404")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Endpoint: GET /api/v1/load-units/status/{status}")
  class FilterLoadUnitsEndpoint {

    @Test
    @DisplayName(
        "Should accept requests and pass correct sorting parameter schemas out to consumer loops")
    void shouldReturnPaginatedListFilteredByStatus() throws Exception {
      LoadUnitResponse response =
          new LoadUnitResponse(1L, "LU-PAGED", 80, LoadUnitStatus.STAGED, 2L, 0L);

      Pageable expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));

      PageImpl<LoadUnitResponse> responsePage =
          new PageImpl<>(List.of(response), expectedPageable, 1);

      when(loadUnitService.findByStatus(LoadUnitStatus.STAGED, expectedPageable))
          .thenReturn(responsePage);

      mockMvc
          .perform(
              get("/api/v1/load-units/status/STAGED")
                  .param("page", "0")
                  .param(
                      "size",
                      "20")) // Spring automagically appends sort="id,asc" based on @PageableDefault
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].trackingCode").value("LU-PAGED"))
          .andExpect(jsonPath("$.totalElements").value(1));
    }
  }
}
