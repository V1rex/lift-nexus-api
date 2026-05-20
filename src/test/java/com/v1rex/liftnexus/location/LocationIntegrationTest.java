package com.v1rex.liftnexus.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.location.dto.LocationRequest;
import com.v1rex.liftnexus.location.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        locationRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E: Should save location in database and return 201 when a valid POST is made")
    void shouldRegisterLocationInDatabase_WhenValidPostRequestIsMade() throws Exception {
        LocationRequest request = new LocationRequest(51.5136F, 7.4653F);

        mockMvc.perform(post("/api/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        List<Location> savedLocations = locationRepository.findAll();

        assertEquals(1, savedLocations.size(), "Database should have exactly one location");
        assertEquals(51.5136F, savedLocations.get(0).getLatitude());
        assertEquals(7.4653F, savedLocations.get(0).getLongitude());
    }
}