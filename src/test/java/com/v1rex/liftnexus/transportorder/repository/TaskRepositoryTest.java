/*
package com.v1rex.liftnexus.transportorder.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
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
public class TaskRepositoryTest {

  @Autowired private TransportOrderRepository taskRepository;

  @Autowired private EntityManager entityManager;
  private StorageBin defaultPickStorageBin;
  private StorageBin defaultDeliveryStorageBin;

  @BeforeEach
  void setUp() {
    defaultPickStorageBin = new StorageBin();
    defaultPickStorageBin.setLatitude(51.5142f);
    defaultPickStorageBin.setLongitude(7.4653f);

    defaultDeliveryStorageBin = new StorageBin();
    defaultDeliveryStorageBin.setLatitude(51.5155f);
    defaultDeliveryStorageBin.setLongitude(7.4668f);

    entityManager.persist(defaultPickStorageBin);
    entityManager.persist(defaultDeliveryStorageBin);
    entityManager.flush();
  }

  @Nested
  @DisplayName("Database Constraint & Validation Tests")
  class ConstraintTests {

    @Test
    @DisplayName("Should throw database exception when weight capacity is zero")
    void shouldThrowException_WhenWeightCapacityIsZero() {
      TransportOrder invalidTask =
          TransportOrder.builder()
              .weight(0)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build();

      assertThatThrownBy(() -> taskRepository.saveAndFlush(invalidTask))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw database exception when weight capacity is negative")
    void shouldThrowException_WhenWeightCapacityIsNegative() {
      TransportOrder invalidTask =
          TransportOrder.builder()
              .weight(-1)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build();

      assertThatThrownBy(() -> taskRepository.saveAndFlush(invalidTask))
          .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should throw database exception when weight capacity is null")
    void shouldThrowException_WhenWeightCapacityIsNull() {
      TransportOrder invalidTask =
          TransportOrder.builder()
              .weight(null)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build();

      assertThatThrownBy(() -> taskRepository.saveAndFlush(invalidTask))
          .isInstanceOf(ConstraintViolationException.class);
    }
  }

  @Nested
  @DisplayName("Custom Query Method Tests")
  class QueryTests {

    @Test
    @DisplayName("Should find transportOrders that do not have an assigned forklift")
    void shouldFindTasksByForkliftIsNull() {
      TransportOrder unassignedTask1 =
          TransportOrder.builder()
              .weight(500)
              .status(TransportOrderStatus.OPEN)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .forklift(null)
              .build();
      TransportOrder unassignedTask2 =
          TransportOrder.builder()
              .weight(800)
              .status(TransportOrderStatus.OPEN)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .forklift(null)
              .build();

      Forklift mockForklift =
          Forklift.builder().weightCapacity(2000).equipmentType(EquipmentType.STANDARD).build();
      entityManager.persist(mockForklift);

      TransportOrder assignedTask =
          TransportOrder.builder()
              .weight(1200)
              .status(TransportOrderStatus.ASSIGNED)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .forklift(mockForklift)
              .build();

      taskRepository.save(unassignedTask1);
      taskRepository.save(unassignedTask2);
      taskRepository.save(assignedTask);
      taskRepository.flush();

      java.util.List<TransportOrder> results = taskRepository.findByForkliftIsNull();

      assertThat(results).hasSize(2);
      assertThat(results).extracting(TransportOrder::getWeight).containsExactlyInAnyOrder(500, 800);
    }

    @Test
    @DisplayName("Should find paged transportOrders matching a specific status")
    void shouldFindTasksByStatusWithPagination() {
      taskRepository.save(
          TransportOrder.builder()
              .weight(100)
              .status(TransportOrderStatus.OPEN)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build());
      taskRepository.save(
          TransportOrder.builder()
              .weight(200)
              .status(TransportOrderStatus.OPEN)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build());
      taskRepository.save(
          TransportOrder.builder()
              .weight(300)
              .status(TransportOrderStatus.COMPLETED)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build());
      taskRepository.flush();

      Pageable pageable = PageRequest.of(0, 10);

      Page<TransportOrder> pendingPage = taskRepository.findByStatus(TransportOrderStatus.OPEN, pageable);

      assertThat(pendingPage.getTotalElements()).isEqualTo(2);
      assertThat(pendingPage.getContent()).allMatch(transportorder -> transportorder.getStatus() == TransportOrderStatus.OPEN);
    }

    @Test
    @DisplayName("Should find paged transportOrders with a weight strictly greater than the threshold")
    void shouldFindTasksWithWeightGreaterThanThreshold() {
      taskRepository.save(
          TransportOrder.builder()
              .weight(500)
              .status(TransportOrderStatus.OPEN)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build());
      taskRepository.save(
          TransportOrder.builder()
              .weight(1000)
              .status(TransportOrderStatus.OPEN)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build());
      taskRepository.save(
          TransportOrder.builder()
              .weight(1500)
              .status(TransportOrderStatus.OPEN)
              .pickStorageBin(defaultPickStorageBin)
              .deliveryStorageBin(defaultDeliveryStorageBin)
              .build());
      taskRepository.flush();

      Pageable pageable = PageRequest.of(0, 10);

      Page<TransportOrder> results = taskRepository.findByWeightGreaterThan(999, pageable);

      assertThat(results.getTotalElements()).isEqualTo(2);
      var weights = results.getContent().stream().map(TransportOrder::getWeight).toList();
      assertThat(weights).containsExactlyInAnyOrder(1000, 1500);
    }

    @Nested
    @DisplayName("JPQL Dynamic Search Tests (searchTasks)")
    class SearchTasksTests {

      @Test
      @DisplayName(
          "Should filter by both status and minimum weight when both parameters are provided")
      void shouldSearchByStatusAndMinWeight() {
        taskRepository.save(
            TransportOrder.builder()
                .weight(500)
                .status(TransportOrderStatus.OPEN)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.save(
            TransportOrder.builder()
                .weight(1500)
                .status(TransportOrderStatus.OPEN)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.save(
            TransportOrder.builder()
                .weight(2000)
                .status(TransportOrderStatus.COMPLETED)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.flush();

        Pageable pageable = PageRequest.of(0, 10);

        Page<TransportOrder> result = taskRepository.searchTasks(TransportOrderStatus.OPEN, 1000, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getWeight()).isEqualTo(1500);
      }

      @Test
      @DisplayName(
          "Should ignore status filter and only filter by minimum weight when status is null")
      void shouldSearchByMinWeightOnly_WhenStatusIsNull() {
        taskRepository.save(
            TransportOrder.builder()
                .weight(500)
                .status(TransportOrderStatus.OPEN)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.save(
            TransportOrder.builder()
                .weight(1200)
                .status(TransportOrderStatus.OPEN)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.save(
            TransportOrder.builder()
                .weight(1800)
                .status(TransportOrderStatus.COMPLETED)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.flush();

        Pageable pageable = PageRequest.of(0, 10);

        Page<TransportOrder> result = taskRepository.searchTasks(null, 1000, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
      }

      @Test
      @DisplayName(
          "Should return all transportOrders within page limits when all query criteria parameters are null")
      void shouldReturnAllTasks_WhenAllParametersAreNull() {
        taskRepository.save(
            TransportOrder.builder()
                .weight(500)
                .status(TransportOrderStatus.OPEN)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.save(
            TransportOrder.builder()
                .weight(1500)
                .status(TransportOrderStatus.COMPLETED)
                .pickStorageBin(defaultPickStorageBin)
                .deliveryStorageBin(defaultDeliveryStorageBin)
                .build());
        taskRepository.flush();

        Pageable pageable = PageRequest.of(0, 10);

        Page<TransportOrder> result = taskRepository.searchTasks(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
      }
    }
  }
}
*/
