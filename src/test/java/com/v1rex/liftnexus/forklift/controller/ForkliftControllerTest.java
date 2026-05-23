/*
package com.v1rex.liftnexus.forklift.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.dto.ForkliftLocationUpdateRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.forklift.service.ForkliftService;
import com.v1rex.liftnexus.transportorder.dto.TransportOrderResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ForkliftController.class)
@ActiveProfiles("test")
public class ForkliftControllerTest {

  @Autowired private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private ForkliftService forkliftService;

  @Nested
  @DisplayName("Tests - GET /api/v1/forklifts/{id}")
  class FindByIdTest {

    @Test
    @DisplayName("Should return a Forklift when GET request is made to /api/v1/forklifts/{id}")
    void shouldReturnForklift_WhenGetRequestIsMadeToFindById() throws Exception {
      ForkliftResponse mockDto =
          new ForkliftResponse(1L, 40, EquipmentType.STANDARD, new ArrayList<TransportOrderResponse>(), null);

      when(forkliftService.findById(1L)).thenReturn(mockDto);

      mockMvc
          .perform(get("/api/v1/forklifts/1"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.weightCapacity").value(40))
          .andExpect(jsonPath("$.equipmentType").value("STANDARD"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when entity missing")
    void shouldReturnNotFound_WhenGetRequestIsMadeToFindByIdWithNonExistingId() throws Exception {
      Long nonExistingId = 1L;

      when(forkliftService.findById(nonExistingId))
          .thenThrow(
              new ResourceNotFoundException("Forklift with " + nonExistingId + " not found."));

      mockMvc
          .perform(get("/api/v1/forklifts/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/forklifts")
  class FindAllForkliftsTest {

    @Test
    @DisplayName("Should return a paged list of forklifts")
    void shouldReturnForklifts_WhenGetRequestIsMade() throws Exception {
      ForkliftResponse mockForkliftOne =
          new ForkliftResponse(1L, 40, EquipmentType.STANDARD, new ArrayList<>(), null);

      ForkliftResponse mockForkliftTwo =
          new ForkliftResponse(2L, 50, EquipmentType.STANDARD, new ArrayList<>(), null);

      Page<ForkliftResponse> mockPage = new PageImpl<>(List.of(mockForkliftOne, mockForkliftTwo));

      when(forkliftService.findAll(Mockito.any(Pageable.class))).thenReturn(mockPage);

      mockMvc
          .perform(get("/api/v1/forklifts").accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(1))
          .andExpect(jsonPath("$.content[1].id").value(2));
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/forklifts/search")
  class SearchByTest {

    @Test
    @DisplayName("Should return filtered paged data when minCapacity is valid")
    void shouldReturnFilteredPagedForklifts() throws Exception {
      ForkliftResponse heavyForklift =
          new ForkliftResponse(3L, 2000, EquipmentType.STANDARD, new ArrayList<>(), null);

      Page<ForkliftResponse> mockPage = new PageImpl<>(List.of(heavyForklift));

      when(forkliftService.findWithCapacityGreaterThan(
              Mockito.eq(1000), Mockito.any(Pageable.class)))
          .thenReturn(mockPage);

      mockMvc
          .perform(
              get("/api/v1/forklifts/search")
                  .param("minCapacity", "1000")
                  .accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(3))
          .andExpect(jsonPath("$.content[0].weightCapacity").value(2000));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when query parameter minCapacity violates @Min(1)")
    void shouldReturnBadRequest_WhenMinCapacityIsZero() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/forklifts/search")
                  .param("minCapacity", "0")
                  .accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Tests - POST /api/v1/forklifts")
  class CreateForkliftTest {

    @Test
    @DisplayName("Should return 201 Created along with matching storagebin headers")
    void shouldCreateForkliftAndReturnCreated() throws Exception {
      ForkliftRequest requestPayload = new ForkliftRequest(1500, EquipmentType.STANDARD);
      ForkliftResponse generatedResponse =
          new ForkliftResponse(99L, 1500, EquipmentType.STANDARD, new ArrayList<>(), null);

      when(forkliftService.createForklift(Mockito.any(ForkliftRequest.class)))
          .thenReturn(generatedResponse);

      mockMvc
          .perform(
              post("/api/v1/forklifts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestPayload)))
          .andDo(print())
          .andExpect(status().isCreated())
          .andExpect(header().string("StorageBin", "http://localhost/api/v1/forklifts/99"))
          .andExpect(jsonPath("$.id").value(99))
          .andExpect(jsonPath("$.weightCapacity").value(1500));
    }
  }

  @Nested
  @DisplayName("Tests - PUT /api/v1/forklifts/{id}/storagebin")
  class UpdateStorageBinTest {

    @Test
    @DisplayName("Should update forklift coordinates storagebin and return 200 OK")
    void shouldUpdateForkliftLocationAndReturnOk() throws Exception {
      ForkliftLocationUpdateRequest updateRequest = new ForkliftLocationUpdateRequest(5L);
      ForkliftResponse updatedResponse =
          new ForkliftResponse(1L, 40, EquipmentType.STANDARD, new ArrayList<>(), null);

      when(forkliftService.updateForkliftLocation(Mockito.eq(1L), Mockito.eq(5L)))
          .thenReturn(updatedResponse);

      mockMvc
          .perform(
              put("/api/v1/forklifts/1/location")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1));
    }
  }
}
*/
