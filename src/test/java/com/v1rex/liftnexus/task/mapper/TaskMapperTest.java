/*
package com.v1rex.liftnexus.task.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.dto.StorageBinResponse;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.dto.TaskRequest;
import com.v1rex.liftnexus.task.dto.TaskResponse;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskMapperTest {

  @Mock private StorageBinMapper storageBinMapper;

  @InjectMocks private TaskMapper mapper;

  @Nested
  @DisplayName("Tests for toEntity mapping")
  class ToEntityTests {

    @Test
    @DisplayName("Should correctly map TaskRequest to Task Entity")
    void shouldMapRequestToEntity() {
      TaskRequest request = new TaskRequest(1L, 2L, TaskStatus.OPEN, EquipmentType.STANDARD, 100);

      Task entity = mapper.toEntity(request);

      assertNotNull(entity);

      assertNull(entity.getId(), "New entities mapped from a request should not have an ID yet");

      assertNull(
          entity.getPickStorageBin(),
          "New entities mapped from a request should not have a pick storagebin yet, as it will be set later by the service");

      assertNull(
          entity.getDeliveryStorageBin(),
          "New entities mapped from a request should not have a delivery storagebin yet, as it will be set later by the service");

      assertNull(
          entity.getForklift(),
          "New entities mapped from a request should not have an assigned forklift yet, as it will be set later by the service");

      assertEquals(request.weight(), entity.getWeight());
      assertEquals(request.status(), entity.getStatus());
      assertEquals(request.requiredEquipment(), entity.getRequiredEquipment());
    }

    @Test
    @DisplayName("Should return null when TaskRequest is null")
    void shouldReturnNull_WhenRequestIsNull() {
      Task entity = mapper.toEntity(null);

      assertNull(entity);
    }
  }

  @Nested
  @DisplayName("Tests for toResponse mapping")
  class ToResponseTests {

    @Test
    @DisplayName("Should correctly map Task Entity to TaskResponse DTO")
    void shouldMapEntityToResponse() {
      // Arranging
      StorageBin mockPickStorageBin =
          StorageBin.builder().id(10L).latitude(10.5F).longitude(13.45F).build();

      StorageBin mockDeliveryStorageBin =
          StorageBin.builder().id(2L).latitude(11.5F).longitude(12.45F).build();

      Forklift mockForlift = Forklift.builder().id(5L).weightCapacity(100).build();

      Task mockTask =
          Task.builder()
              .id(15L)
              .pickStorageBin(mockPickStorageBin)
              .deliveryStorageBin(mockDeliveryStorageBin)
              .weight(10)
              .requiredEquipment(EquipmentType.STANDARD)
              .status(TaskStatus.OPEN)
              .forklift(mockForlift)
              .build();

      StorageBinResponse mockPickStorageBinResponse = new StorageBinResponse(1L, 10.5F, 13.45F);

      StorageBinResponse mockDeliveryStorageBinResponse = new StorageBinResponse(2L, 11.5F, 12.45F);

      when(storageBinMapper.toResponse(mockPickStorageBin)).thenReturn(mockPickStorageBinResponse);

      when(storageBinMapper.toResponse(mockDeliveryStorageBin))
          .thenReturn(mockDeliveryStorageBinResponse);

      // Act
      var response = mapper.toResponse(mockTask);

      // Assert
      assertNotNull(response);
      assertEquals(15L, response.id());
      assertEquals(mockPickStorageBinResponse, response.pickLocation());
      assertEquals(mockDeliveryStorageBinResponse, response.deliveryLocation());
      assertEquals(10, response.weight());
      assertEquals(EquipmentType.STANDARD, response.requiredEquipment());
      assertEquals(5L, response.forkliftId());
    }

    @Test
    @DisplayName("Should return null when Task Entity is null")
    void shouldReturnNull_WhenEntityIsNull() {
      TaskResponse response = mapper.toResponse(null);
      assertNull(response);
    }
  }
}
*/
