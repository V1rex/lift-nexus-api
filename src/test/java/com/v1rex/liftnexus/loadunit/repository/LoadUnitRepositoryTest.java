package com.v1rex.liftnexus.loadunit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.v1rex.liftnexus.config.TestContainersConfiguration;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@DisplayName("LoadUnitRepository Integration Tests")
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
class LoadUnitRepositoryTest {

  @Autowired private LoadUnitRepository loadUnitRepository;

  @Autowired private TestEntityManager entityManager;

  @Nested
  @DisplayName("Validation Constraints Tests")
  class ValidationConstraints {

    @Test
    @DisplayName("Should fail when tracking code is blank")
    void shouldFailWhenTrackingCodeIsBlank() {
      LoadUnit invalidUnit =
          LoadUnit.builder()
              .trackingCode("   ")
              .weightKg(500)
              .status(LoadUnitStatus.EXPECTED)
              .build();

      assertThatThrownBy(() -> entityManager.persistAndFlush(invalidUnit))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("Tracking code must be provided");
    }

    @Test
    @DisplayName("Should fail when weight is negative")
    void shouldFailWhenWeightIsNegative() {
      LoadUnit invalidUnit =
          LoadUnit.builder()
              .trackingCode("LU-NEGATIVE")
              .weightKg(-1)
              .status(LoadUnitStatus.EXPECTED)
              .build();

      assertThatThrownBy(() -> entityManager.persistAndFlush(invalidUnit))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("Weight cannot be negative");
    }

    @Test
    @DisplayName("Should enforce global database uniqueness constraint on tracking code")
    void shouldEnforceUniquenessOnTrackingCode() {
      LoadUnit continuousUnit1 =
          LoadUnit.builder()
              .trackingCode("LU-DUPLICATE-123")
              .weightKg(350)
              .status(LoadUnitStatus.STAGED)
              .build();
      entityManager.persistAndFlush(continuousUnit1);

      LoadUnit continuousUnit2 =
          LoadUnit.builder()
              .trackingCode("LU-DUPLICATE-123")
              .weightKg(400)
              .status(LoadUnitStatus.STORED)
              .build();

      assertThatThrownBy(() -> entityManager.persistAndFlush(continuousUnit2))
          .isInstanceOf(PersistenceException.class);
    }
  }

  @Nested
  @DisplayName("Custom Domain Queries Tests")
  class CustomQueries {

    @Test
    @DisplayName("Should properly verify existence by tracking code")
    void shouldVerifyExistenceByTrackingCode() {
      LoadUnit unit =
          LoadUnit.builder()
              .trackingCode("LU-EXISTS-999")
              .weightKg(120)
              .status(LoadUnitStatus.STORED)
              .build();
      entityManager.persistAndFlush(unit);

      assertThat(loadUnitRepository.existsByTrackingCode("LU-EXISTS-999")).isTrue();
      assertThat(loadUnitRepository.existsByTrackingCode("LU-NON-EXISTENT")).isFalse();
    }

    @Test
    @DisplayName("Should retrieve correct optional entity structure by tracking code")
    void shouldRetrieveOptionalByTrackingCode() {
      LoadUnit unit =
          LoadUnit.builder()
              .trackingCode("LU-FIND-777")
              .weightKg(850)
              .status(LoadUnitStatus.IN_TRANSIT)
              .build();
      entityManager.persistAndFlush(unit);

      Optional<LoadUnit> found = loadUnitRepository.findByTrackingCode("LU-FIND-777");
      assertThat(found).isPresent();
      assertThat(found.get().getWeightKg()).isEqualTo(850);
      assertThat(found.get().getStatus()).isEqualTo(LoadUnitStatus.IN_TRANSIT);

      Optional<LoadUnit> notFound = loadUnitRepository.findByTrackingCode("LU-ABSENT");
      assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Should fetch paginated elements matching specific lifecycle statuses")
    void shouldFetchPaginatedByStatus() {
      entityManager.persist(
          LoadUnit.builder()
              .trackingCode("LU-STAT-1")
              .weightKg(100)
              .status(LoadUnitStatus.STAGED)
              .build());
      entityManager.persist(
          LoadUnit.builder()
              .trackingCode("LU-STAT-2")
              .weightKg(200)
              .status(LoadUnitStatus.STAGED)
              .build());
      entityManager.persist(
          LoadUnit.builder()
              .trackingCode("LU-STAT-3")
              .weightKg(300)
              .status(LoadUnitStatus.SHIPPED)
              .build());
      entityManager.flush();

      Page<LoadUnit> stagedPage =
          loadUnitRepository.findByStatus(LoadUnitStatus.STAGED, PageRequest.of(0, 10));

      assertThat(stagedPage.getContent()).hasSize(2);
      assertThat(stagedPage.getContent())
          .extracting(LoadUnit::getTrackingCode)
          .containsExactlyInAnyOrder("LU-STAT-1", "LU-STAT-2");
    }
  }
}
