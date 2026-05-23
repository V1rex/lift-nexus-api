package com.v1rex.liftnexus.storagebin.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class StorageBinRepositoryTest {

  @Autowired private LocationRepository locationRepository;

  @Test
  @DisplayName("Should save and find a StorageBin by ID")
  void shouldSaveAndFindLocation() {
    StorageBin storageBin = StorageBin.builder().latitude(51.5136F).longitude(7.4653F).build();

    StorageBin saved = locationRepository.save(storageBin);

    Optional<StorageBin> found = locationRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getLatitude()).isEqualTo(storageBin.getLatitude());
    assertThat(found.get().getLongitude()).isEqualTo(storageBin.getLongitude());
  }

  @Test
  @DisplayName("Should return empty Optional when StorageBin does not exist")
  void shouldReturnEmptyOptional_WhenLocationDoesNotExist() {
    Long nonExistingId = 99999L;

    Optional<StorageBin> found = locationRepository.findById(nonExistingId);

    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should throw exception when required fields are missing")
  void shouldThrowException_WhenRequiredFieldsAreMissing() {
    StorageBin invalidStorageBin = StorageBin.builder().latitude(51.5136f).longitude(null).build();

    assertThatThrownBy(
            () -> {
              locationRepository.saveAndFlush(invalidStorageBin);
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
