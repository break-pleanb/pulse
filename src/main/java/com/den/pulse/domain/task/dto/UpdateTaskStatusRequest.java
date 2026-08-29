package com.den.pulse.domain.task.dto;

import com.den.pulse.domain.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "status는 필수입니다.")
        TaskStatus status
) {
}
