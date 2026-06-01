package com.v1rex.liftnexus.forklift.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.common.exception.GlobalExceptionHandler;
import com.v1rex.liftnexus.common.exception.ProblemDetailFactory;
import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftTypeResponse;
import com.v1rex.liftnexus.forklift.service.ForkliftTypeService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ForkliftTypeController.class)
@Import({GlobalExceptionHandler.class, ProblemDetailFactory.class})
public class ForkliftTypeControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private ForkliftTypeService forkliftTypeService;

  @Nested
  @DisplayName("Tests - POST /api/v1/forklift-types")
  class CreateForkliftTypeTest {

    @Test
    @DisplayName("Should return 201 Created and Location header")
    void shouldCreateAndReturn201() throws Exception {
      ForkliftTypeRequest request =
          new ForkliftTypeRequest("Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);
      ForkliftTypeResponse response =
          new ForkliftTypeResponse(10L, "Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);

      when(forkliftTypeService.createForkliftType(any(ForkliftTypeRequest.class)))
          .thenReturn(response);

      mockMvc
          .perform(
              post("/api/v1/forklift-types")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "http://localhost/api/v1/forklift-types/10"))
          .andExpect(jsonPath("$.id").value(10))
          .andExpect(jsonPath("$.modelName").value("Toyota X"));
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/forklift-types/{id}")
  class GetForkliftTypeByIdTest {

    @Test
    @DisplayName("Should return 200 OK with requested blueprint")
    void shouldReturnById() throws Exception {
      ForkliftTypeResponse response =
          new ForkliftTypeResponse(1L, "Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);

      when(forkliftTypeService.findById(1L)).thenReturn(response);

      mockMvc
          .perform(get("/api/v1/forklift-types/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.modelName").value("Toyota X"));
    }
  }

  @Nested
  @DisplayName("Tests - GET /api/v1/forklift-types")
  class GetAllForkliftTypesTest {

    @Test
    @DisplayName("Should return 200 OK with paginated list")
    void shouldReturnPaginatedList() throws Exception {
      ForkliftTypeResponse response =
          new ForkliftTypeResponse(1L, "Toyota X", EquipmentType.STANDARD, 2000, 50.0, 0.5);
      Page<ForkliftTypeResponse> page = new PageImpl<>(List.of(response));

      when(forkliftTypeService.findAll(any(Pageable.class))).thenReturn(page);

      mockMvc
          .perform(get("/api/v1/forklift-types"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(1))
          .andExpect(jsonPath("$.content[0].modelName").value("Toyota X"));
    }
  }
}
