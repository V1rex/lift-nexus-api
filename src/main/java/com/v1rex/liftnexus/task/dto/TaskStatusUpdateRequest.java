package com.v1rex.liftnexus.task.dto;

import com.v1rex.liftnexus.task.enums.TaskStatus;

public record TaskStatusUpdateRequest(TaskStatus status) {
}
