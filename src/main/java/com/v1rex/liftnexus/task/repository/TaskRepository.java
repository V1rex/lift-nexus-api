package com.v1rex.liftnexus.task.repository;

import com.v1rex.liftnexus.task.domain.Task;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
  List<Task> findByForkliftIsNull();

  Page<Task> findByStatus(TaskStatus status, Pageable pageable);

  Page<Task> findByWeightGreaterThan(
      @NotNull @Min(value = 1, message = "Weight must be greater than 0")
          Integer weightIsGreaterThan,
      Pageable pageable);

  @Query(
      """
        SELECT t FROM Task t
        WHERE (:status IS NULL OR t.status = :status)
        AND (:minWeight IS NULL OR t.weight >= :minWeight)
    """)
  Page<Task> searchTasks(
      @Param("status") TaskStatus status, @Param("minWeight") Integer minWeight, Pageable pageable);
}
