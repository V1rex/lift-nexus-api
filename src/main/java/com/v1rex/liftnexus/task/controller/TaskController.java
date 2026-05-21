package com.v1rex.liftnexus.task.controller;

import com.v1rex.liftnexus.task.dto.TaskRequest;
import com.v1rex.liftnexus.task.dto.TaskResponse;
import com.v1rex.liftnexus.task.dto.TaskStatusUpdateRequest;
import com.v1rex.liftnexus.task.enums.TaskStatus;
import com.v1rex.liftnexus.task.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/tasks")
@Validated
@RequiredArgsConstructor
public class TaskController {
  private final TaskService taskService;

  @GetMapping("/{id}")
  public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
    return ResponseEntity.ok(taskService.findById(id));
  }

  @GetMapping("/search")
  public ResponseEntity<Page<TaskResponse>> searchTasks(
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) @Min(1) Integer minWeight,
      @PageableDefault(size = 10, sort = "weight") Pageable pageable) {
    return ResponseEntity.ok(taskService.searchTasks(status, minWeight, pageable));
  }

  @PostMapping
  public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid TaskRequest taskRequest) {
    TaskResponse savedPickTask = taskService.createTask(taskRequest);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedPickTask.id())
            .toUri();

    return ResponseEntity.created(location).body(savedPickTask);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TaskResponse> updateTaskStatus(
      @PathVariable Long id, @RequestBody TaskStatusUpdateRequest newStatusRequest) {
    TaskResponse updatedTask = taskService.updateTask(id, newStatusRequest);

    return ResponseEntity.ok(updatedTask);
  }
}
