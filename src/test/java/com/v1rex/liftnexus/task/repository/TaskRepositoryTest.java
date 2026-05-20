package com.v1rex.liftnexus.task.repository;

import com.v1rex.liftnexus.forklift.domain.EquipmentType;
import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.location.domain.Location;
import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.enums.TaskStatus;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;
    private Location defaultPickLocation;
    private Location defaultDeliveryLocation;

    @BeforeEach
    void setUp() {
        defaultPickLocation = new Location();
        defaultPickLocation.setLatitude(51.5142f);
        defaultPickLocation.setLongitude(7.4653f);

        defaultDeliveryLocation = new Location();
        defaultDeliveryLocation.setLatitude(51.5155f);
        defaultDeliveryLocation.setLongitude(7.4668f);


        entityManager.persist(defaultPickLocation);
        entityManager.persist(defaultDeliveryLocation);
        entityManager.flush();
    }

    @Nested
    @DisplayName("Database Constraint & Validation Tests")
    class ConstraintTests {

        @Test
        @DisplayName("Should throw database exception when weight capacity is zero")
        void shouldThrowException_WhenWeightCapacityIsZero() {
            Task invalidTask = Task.builder()
                    .weight(0)
                    .pickLocation(defaultPickLocation)
                    .deliveryLocation(defaultDeliveryLocation)
                    .build();

            assertThatThrownBy(() -> taskRepository.saveAndFlush(invalidTask))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should throw database exception when weight capacity is negative")
        void shouldThrowException_WhenWeightCapacityIsNegative() {
            Task invalidTask = Task.builder()
                    .weight(-1)
                    .pickLocation(defaultPickLocation)
                    .deliveryLocation(defaultDeliveryLocation)
                    .build();

            assertThatThrownBy(() -> taskRepository.saveAndFlush(invalidTask))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should throw database exception when weight capacity is null")
        void shouldThrowException_WhenWeightCapacityIsNull() {
            Task invalidTask = Task.builder()
                    .weight(null)
                    .pickLocation(defaultPickLocation)
                    .deliveryLocation(defaultDeliveryLocation)
                    .build();

            assertThatThrownBy(() -> taskRepository.saveAndFlush(invalidTask))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("Custom Query Method Tests")
    class QueryTests {

        @Test
        @DisplayName("Should find tasks that do not have an assigned forklift")
        void shouldFindTasksByForkliftIsNull() {
            Task unassignedTask1 = Task.builder().weight(500).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).forklift(null).build();
            Task unassignedTask2 = Task.builder().weight(800).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).forklift(null).build();


            Forklift mockForklift = Forklift.builder()
                                                .weightCapacity(2000)
                                                .equipmentType(EquipmentType.STANDARD)
                                                .build();
            entityManager.persist(mockForklift);

            Task assignedTask = Task.builder().weight(1200).status(TaskStatus.ASSIGNED).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).forklift(mockForklift).build();

            taskRepository.save(unassignedTask1);
            taskRepository.save(unassignedTask2);
            taskRepository.save(assignedTask);
            taskRepository.flush();

            java.util.List<Task> results = taskRepository.findByForkliftIsNull();

            assertThat(results).hasSize(2);
            assertThat(results).extracting(Task::getWeight).containsExactlyInAnyOrder(500, 800);
        }

        @Test
        @DisplayName("Should find paged tasks matching a specific status")
        void shouldFindTasksByStatusWithPagination() {
            taskRepository.save(Task.builder().weight(100).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
            taskRepository.save(Task.builder().weight(200).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
            taskRepository.save(Task.builder().weight(300).status(TaskStatus.COMPLETED).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
            taskRepository.flush();

            Pageable pageable = PageRequest.of(0, 10);

            Page<Task> pendingPage = taskRepository.findByStatus(TaskStatus.OPEN, pageable);

            assertThat(pendingPage.getTotalElements()).isEqualTo(2);
            assertThat(pendingPage.getContent()).allMatch(task -> task.getStatus() == TaskStatus.OPEN);
        }

        @Test
        @DisplayName("Should find paged tasks with a weight strictly greater than the threshold")
        void shouldFindTasksWithWeightGreaterThanThreshold() {
            taskRepository.save(Task.builder().weight(500).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
            taskRepository.save(Task.builder().weight(1000).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
            taskRepository.save(Task.builder().weight(1500).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
            taskRepository.flush();

            Pageable pageable = PageRequest.of(0, 10);

            Page<Task> results = taskRepository.findByWeightGreaterThan(999, pageable);

            assertThat(results.getTotalElements()).isEqualTo(2);
            var weights = results.getContent().stream().map(Task::getWeight).toList();
            assertThat(weights).containsExactlyInAnyOrder(1000, 1500);
        }

        @Nested
        @DisplayName("JPQL Dynamic Search Tests (searchTasks)")
        class SearchTasksTests {

            @Test
            @DisplayName("Should filter by both status and minimum weight when both parameters are provided")
            void shouldSearchByStatusAndMinWeight() {
                taskRepository.save(Task.builder().weight(500).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.save(Task.builder().weight(1500).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.save(Task.builder().weight(2000).status(TaskStatus.COMPLETED).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.flush();

                Pageable pageable = PageRequest.of(0, 10);

                Page<Task> result = taskRepository.searchTasks(TaskStatus.OPEN, 1000, pageable);

                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getContent().get(0).getWeight()).isEqualTo(1500);
            }

            @Test
            @DisplayName("Should ignore status filter and only filter by minimum weight when status is null")
            void shouldSearchByMinWeightOnly_WhenStatusIsNull() {
                taskRepository.save(Task.builder().weight(500).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.save(Task.builder().weight(1200).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.save(Task.builder().weight(1800).status(TaskStatus.COMPLETED).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.flush();

                Pageable pageable = PageRequest.of(0, 10);

                Page<Task> result = taskRepository.searchTasks(null, 1000, pageable);

                assertThat(result.getTotalElements()).isEqualTo(2);
            }

            @Test
            @DisplayName("Should return all tasks within page limits when all query criteria parameters are null")
            void shouldReturnAllTasks_WhenAllParametersAreNull() {
                taskRepository.save(Task.builder().weight(500).status(TaskStatus.OPEN).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.save(Task.builder().weight(1500).status(TaskStatus.COMPLETED).pickLocation(defaultPickLocation).deliveryLocation(defaultDeliveryLocation).build());
                taskRepository.flush();

                Pageable pageable = PageRequest.of(0, 10);

                Page<Task> result = taskRepository.searchTasks(null, null, pageable);

                assertThat(result.getTotalElements()).isEqualTo(2);
            }
        }
    }
}