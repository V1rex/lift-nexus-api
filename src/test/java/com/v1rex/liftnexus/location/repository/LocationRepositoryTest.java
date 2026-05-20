package com.v1rex.liftnexus.location.repository;

import com.v1rex.liftnexus.location.domain.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;


import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Test
    @DisplayName("Should save and find a Location by ID")
    void shouldSaveAndFindLocation() {
        Location location = Location.builder()
                .latitude(51.5136F)
                .longitude(7.4653F)
                .build();


        Location saved = locationRepository.save(location);


        Optional<Location> found = locationRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLatitude()).isEqualTo(location.getLatitude());
        assertThat(found.get().getLongitude()).isEqualTo(location.getLongitude());
    }

    @Test
    @DisplayName("Should return empty Optional when Location does not exist")
    void shouldReturnEmptyOptional_WhenLocationDoesNotExist() {
        Long nonExistingId = 99999L;


        Optional<Location> found = locationRepository.findById(nonExistingId);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when required fields are missing")
    void shouldThrowException_WhenRequiredFieldsAreMissing() {
        Location invalidLocation = Location.builder()
                .latitude(51.5136f)
                .longitude(null)
                .build();


        assertThatThrownBy(() -> {
            locationRepository.saveAndFlush(invalidLocation);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
