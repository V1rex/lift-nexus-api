package com.v1rex.liftnexus.storagebin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.common.exception.GlobalExceptionHandler;
import com.v1rex.liftnexus.common.exception.ProblemDetailFactory;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import com.v1rex.liftnexus.storagebin.dto.CoordinateDto;
import com.v1rex.liftnexus.storagebin.dto.StorageBinRequest;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.exception.StorageBinNotFoundException;
import com.v1rex.liftnexus.storagebin.service.StorageBinService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StorageBinController.class)
@Import({
  GlobalExceptionHandler.class,
  StorageBinExceptionHandler.class,
  ProblemDetailFactory.class
})
@ActiveProfiles("test")
@DisplayName("StorageBin REST API Gateway Endpoints Tests")
public class StorageBinControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private StorageBinService storageBinService;

  @Nested
  @DisplayName("Query Endpoints (GET Operations)")
  class ReadOperations {
    @Test
    @DisplayName("GET /api/v1/storage-bins should return nested structural arrays")
    void shouldReturnStorageBinsPaginated() throws Exception {
      CoordinateDto coordinate = new CoordinateDto(4, 12, 2);
      StorageBinResponse response =
          new StorageBinResponse(1L, "A-04-B-12-T-02", coordinate, ZoneType.STORAGE, 1000);

      Mockito.when(storageBinService.findAll(any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(response)));

      mockMvc
          .perform(get("/api/v1/storage-bins").accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].binCode").value("A-04-B-12-T-02"))
          .andExpect(jsonPath("$.content[0].coordinate.x").value(4))
          .andExpect(jsonPath("$.content[0].coordinate.z").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/storage-bins/{id} should return 200 OK and the requested bin")
    void shouldReturnStorageBin_WhenIdExists() throws Exception {
      // Arrange
      CoordinateDto coordinate = new CoordinateDto(2, 5, 1);
      StorageBinResponse response =
          new StorageBinResponse(99L, "B-02-B-05-T-01", coordinate, ZoneType.STORAGE, 1500);

      Mockito.when(storageBinService.findById(99L)).thenReturn(response);

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/storage-bins/{id}", 99L).accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(99))
          .andExpect(jsonPath("$.binCode").value("B-02-B-05-T-01"))
          .andExpect(jsonPath("$.coordinate.x").value(2))
          .andExpect(jsonPath("$.coordinate.y").value(5))
          .andExpect(jsonPath("$.coordinate.z").value(1))
          .andExpect(jsonPath("$.zoneType").value("STORAGE"));
    }

    @Test
    @DisplayName("GET /api/v1/storage-bins/{id} should return 404 Not Found if missing")
    void shouldReturn404_WhenStorageBinDoesNotExist() throws Exception {
      // Arrange
      Long missingId = 999L;
      Mockito.when(storageBinService.findById(missingId))
          .thenThrow(new StorageBinNotFoundException(missingId));

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/storage-bins/{id}", missingId).accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Query Endpoints (POST Operations)")
  class WriteOperations {

    @Test
    @DisplayName(
        "POST /api/v1/storage-bins should process complex inputs and output 201 HTTP headers")
    void shouldCreateStorageBin_WhenPayloadIsValid() throws Exception {
      CoordinateDto coordinate = new CoordinateDto(1, 1, 0);
      StorageBinRequest request =
          new StorageBinRequest("CHARGER-1", coordinate, ZoneType.CHARGING_STATION, 0);
      StorageBinResponse response =
          new StorageBinResponse(77L, "CHARGER-1", coordinate, ZoneType.CHARGING_STATION, 0);

      Mockito.when(storageBinService.createStorageBin(any(StorageBinRequest.class)))
          .thenReturn(response);

      mockMvc
          .perform(
              post("/api/v1/storage-bins")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isCreated())
          .andExpect(
              header()
                  .string(
                      "Location", org.hamcrest.Matchers.containsString("/api/v1/storage-bins/77")))
          .andExpect(jsonPath("$.id").value(77))
          .andExpect(jsonPath("$.maxWeightCapacityKg").value(0));
    }

    @Test
    @DisplayName(
        "POST /api/v1/storage-bins should return 400 Bad Request if coordinates are malformed")
    void shouldRejectCreation_WhenPayloadIsMissingCoordinates() throws Exception {
      // Missing the nested CoordinateDto entirely
      String invalidJson =
          "{\"binCode\":\"ERROR\",\"zoneType\":\"STORAGE\",\"maxWeightCapacityKg\":500}";

      mockMvc
          .perform(
              post("/api/v1/storage-bins")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidJson))
          .andDo(print())
          .andExpect(status().isBadRequest());

      Mockito.verifyNoInteractions(storageBinService);
    }
  }
}
