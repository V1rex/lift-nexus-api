package com.v1rex.liftnexus.planning.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.v1rex.liftnexus.planning.domain.DispatchJob;
import com.v1rex.liftnexus.planning.domain.JobStatus;
import com.v1rex.liftnexus.planning.dto.DispatchJobResponse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DispatchJobMapper Unit Tests")
public class DispatchJobMapperTest {

  private final DispatchJobMapper mapper = new DispatchJobMapper();

  @Nested
  @DisplayName("Feature: Map Entity to Response DTO")
  class ToResponse {

    @Test
    @DisplayName("Should return null when the source entity is null")
    void shouldReturnNullWhenEntityIsNull() {

      DispatchJobResponse response = mapper.toResponse(null);

      assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should completely copy all fields from Entity to Record DTO")
    void shouldMapAllFieldsCorrectly() {

      UUID expectedId = UUID.randomUUID();
      Instant expectedCreatedAt = Instant.now().minusSeconds(60);
      Instant expectedCompletedAt = Instant.now();
      String expectedScore = "0hard/-120soft";

      DispatchJob entity =
          DispatchJob.builder()
              .id(expectedId)
              .status(JobStatus.COMPLETED)
              .createdAt(expectedCreatedAt)
              .completedAt(expectedCompletedAt)
              .finalScore(expectedScore)
              .build();

      DispatchJobResponse response = mapper.toResponse(entity);

      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(expectedId);
      assertThat(response.status()).isEqualTo(JobStatus.COMPLETED);
      assertThat(response.createdAt()).isEqualTo(expectedCreatedAt);
      assertThat(response.completedAt()).isEqualTo(expectedCompletedAt);
      assertThat(response.finalScore()).isEqualTo(expectedScore);
    }

    @Test
    @DisplayName("Should successfully map partial entities with missing optional fields")
    void shouldMapPartialEntityWithNullOptionalFields() {

      UUID expectedId = UUID.randomUUID();
      Instant expectedCreatedAt = Instant.now();

      DispatchJob queuedEntity =
          DispatchJob.builder()
              .id(expectedId)
              .status(JobStatus.QUEUED)
              .createdAt(expectedCreatedAt)
              .completedAt(null)
              .finalScore(null)
              .build();

      DispatchJobResponse response = mapper.toResponse(queuedEntity);

      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(expectedId);
      assertThat(response.status()).isEqualTo(JobStatus.QUEUED);
      assertThat(response.createdAt()).isEqualTo(expectedCreatedAt);
      assertThat(response.completedAt()).isNull();
      assertThat(response.finalScore()).isNull();
    }
  }
}
