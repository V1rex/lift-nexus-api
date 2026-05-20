package com.v1rex.liftnexus.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.common.exception.ResourceNotFoundException;
import com.v1rex.liftnexus.location.dto.LocationRequest;
import com.v1rex.liftnexus.location.dto.LocationResponse;
import com.v1rex.liftnexus.location.service.LocationService;
import org.junit.jupiter.api.DisplayName;
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

import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LocationController.class)
@ActiveProfiles("test")
public class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LocationService locationService;



    @Test
    @DisplayName("Should return a list of locations when GET request is made to /api/v1/locations")
    void shouldReturnLocations_WhenGetRequestIsMade() throws Exception {
        LocationResponse mockDto = new LocationResponse(1L,
                51.5136F,
                7.4653F);

        Page<LocationResponse> mockPage = new PageImpl<>(List.of(mockDto));
        Mockito.when(locationService.findAll(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/locations")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].latitude").value(51.5136))
                .andExpect(jsonPath("$.content[0].longitude").value(7.4653));
    }

    @Test
    @DisplayName("Should return a location when GET request is made to /api/v1/locations/{id}")
    void shouldReturnLocation_WhenGetRequestIsMadeToFindById() throws Exception {
        LocationResponse mockDto = new LocationResponse(1L,
                51.5136F,
                7.4653F);

        Mockito.when(locationService.findById(1L)).thenReturn(mockDto);

        mockMvc.perform(get("/api/v1/locations/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.latitude").value(51.5136))
                .andExpect(jsonPath("$.longitude").value(7.4653));

    }

    @Test
    @DisplayName("Should return 404 Not Found when GET request is made to /api/v1/locations/{id} with non-existing id")
    void shouldReturnNotFound_WhenGetRequestIsMadeToFindByIdWithNonExistingId() throws Exception {
        Long nonExistingId = 1L;

        Mockito.when(locationService.findById(nonExistingId))
                .thenThrow(new ResourceNotFoundException("Location with " + nonExistingId + " not found."));

        mockMvc.perform(get("/api/v1/locations/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
@DisplayName("Should return 201 Created and the location URI when valid data is posted")
void shouldCreateLocation_WhenDataIsValid() throws Exception {
    LocationRequest requestDto = new LocationRequest(51.5136F, 7.4653F);
    LocationResponse responseDto = new LocationResponse(42L, 51.5136F, 7.4653F);

    Mockito.when(locationService.createLocation(any(LocationRequest.class)))
            .thenReturn(responseDto);

    mockMvc.perform(post("/api/v1/locations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/locations/42")))
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.latitude").value(51.5136))
            .andExpect(jsonPath("$.longitude").value(7.4653));
}

    @Test
    @DisplayName("Should return 400 Bad Request when validation fails on create")
    void shouldReturnBadRequest_WhenPostRequestContainsInvalidData() throws Exception {
        String invalidJson = "{\"latitude\": 51.5136, \"longitude\": null}";

        mockMvc.perform(post("/api/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(locationService);
    }
}