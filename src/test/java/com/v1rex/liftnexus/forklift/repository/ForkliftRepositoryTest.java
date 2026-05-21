package com.v1rex.liftnexus.forklift.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class ForkliftRepositoryTest {

  @Autowired private ForkliftRepository forkliftRepository;

  @Nested
  @DisplayName("Database Constraint & Validation Tests")
  class ConstraintTests {

    @Test
    @DisplayName("Should throw database exception when weight capacity is zero")
    void shouldThrowException_WhenWeightCapacityIsZero() {
      Forklift invalidForklift = Forklift.builder().weightCapacity(0).build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw database exception when weight capacity is negative")
    void shouldThrowException_WhenWeightCapacityIsNegative() {
      Forklift invalidForklift = Forklift.builder().weightCapacity(-1).build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw database exception when weight capacity is null")
    void shouldThrowException_WhenWeightCapacityIsNull() {
      Forklift invalidForklift = Forklift.builder().weightCapacity(null).build();

      assertThatThrownBy(() -> forkliftRepository.saveAndFlush(invalidForklift))
          .isInstanceOf(ConstraintViolationException.class);
    }
  }

  @Nested
  @DisplayName("Custom Query Method Tests")
  class QueryTests {

    @Test
    @DisplayName("Should correctly filter and paginate forklifts by weight capacity")
    void shouldFindForkliftsWithWeightCapacityGreaterThan() {
      Forklift lightForklift = Forklift.builder().weightCapacity(500).build();
      Forklift mediumForklift = Forklift.builder().weightCapacity(1500).build();
      Forklift heavyForklift = Forklift.builder().weightCapacity(3000).build();

      forkliftRepository.save(lightForklift);
      forkliftRepository.save(mediumForklift);
      forkliftRepository.save(heavyForklift);
      forkliftRepository.flush();

      Pageable pageable = PageRequest.of(0, 10);

      Page<Forklift> result = forkliftRepository.findByWeightCapacityGreaterThan(1000, pageable);

      assertNotNull(result);
      assertEquals(
          2, result.getTotalElements(), "Should match exactly 2 forklifts heavier than 1000");

      var weights = result.getContent().stream().map(Forklift::getWeightCapacity).toList();
      assertThat(weights.contains(500)).isFalse();
      assertThat(weights.contains(1500)).isTrue();
      assertThat(weights.contains(3000)).isTrue();
    }
  }
}
