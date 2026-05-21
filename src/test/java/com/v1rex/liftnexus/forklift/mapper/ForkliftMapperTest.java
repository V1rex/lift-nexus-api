package com.v1rex.liftnexus.forklift.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.dto.ForkliftRequest;
import com.v1rex.liftnexus.forklift.dto.ForkliftResponse;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.location.dto.LocationResponse;
import com.v1rex.liftnexus.location.mapper.LocationMapper;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.dto.TaskResponse;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import com.v1rex.liftnexus.task.mapper.TaskMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ForkliftMapperTest {

  @Mock private TaskMapper taskMapper;

  @Mock private LocationMapper locationMapper;

  @InjectMocks private ForkliftMapper mapper;

  @Nested
  @DisplayName("Tests for toEntity mapping")
  class ToEntityTests {

    @Test
    @DisplayName("Should correctly map LocationRequest to Location Entity")
    void shouldMapRequestToEntity() {
      ForkliftRequest request = new ForkliftRequest(1, EquipmentType.STANDARD);

      Forklift entity = mapper.toEntity(request);

      assertNotNull(entity);
      assertNull(entity.getId(), "New entities mapped from a request should not have an ID yet");
      assertEquals(1, entity.getWeightCapacity());
      assertEquals(EquipmentType.STANDARD, entity.getEquipmentType());
    }

    @Test
    @DisplayName("Should return null when LocationRequest is null")
    void shouldReturnNull_WhenRequestIsNull() {
      Forklift entity = mapper.toEntity(null);

      assertNull(entity);
    }
  }

  @Nested
  @DisplayName("Tests for toResponse mapping")
  class ToResponseTests {
    @Test
    @DisplayName("Should correctly map " + "Forklift Entity to ForkliftResponse DTO")
    void shouldMapEntityToResponse() {
      // Arranging
      Task mockTask1 = Task.builder().id(1L).weight(10).build();

      when(taskMapper.toResponse(mockTask1))
          .thenReturn(
              new TaskResponse(1L, null, null, 10, EquipmentType.STANDARD, TaskStatus.OPEN, null));

      Task mockTask2 = Task.builder().id(2L).weight(5).build();

      when(taskMapper.toResponse(mockTask2))
          .thenReturn(
              new TaskResponse(2L, null, null, 5, EquipmentType.STANDARD, TaskStatus.OPEN, null));

      Location mockLocation =
          Location.builder().id(99L).latitude(51.5136F).longitude(7.4653F).build();
      when(locationMapper.toResponse(mockLocation))
          .thenReturn(new LocationResponse(99L, 51.5136F, 7.4653F));

      List<Task> mockTasks = new ArrayList<>();
      mockTasks.add(mockTask1);
      mockTasks.add(mockTask2);

      Forklift entity =
          Forklift.builder()
              .id(42L)
              .weightCapacity(1)
              .equipmentType(EquipmentType.STANDARD)
              .tasks(mockTasks)
              .currentLocation(mockLocation)
              .build();

      // Acting

      var response = mapper.toResponse(entity);

      // Asserting

      assertNotNull(response);
      assertEquals(42L, response.id());
      assertEquals(1, response.weightCapacity());
      assertEquals(EquipmentType.STANDARD, response.equipmentType());

      // we take a look at the mocked tasks
      assertEquals(2, response.tasks().size());
      assertEquals(1L, response.tasks().get(0).id());
      assertEquals(10, response.tasks().get(0).weight());
      assertEquals(2L, response.tasks().get(1).id());
      assertEquals(5, response.tasks().get(1).weight());

      // we take a look at the mocked location
      assertEquals(99L, response.currentLocation().id());
      assertEquals(51.5136F, response.currentLocation().latitude());
      assertEquals(7.4653F, response.currentLocation().longitude());
    }

    @Test
    @DisplayName("Should return null when Forklift Entity is null")
    void shouldReturnNull_WhenEntityIsNull() {
      ForkliftResponse response = mapper.toResponse(null);
      assertNull(response);
    }
  }
}
