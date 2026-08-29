package com.den.pulse.domain.task.dto;

import com.den.pulse.domain.task.entity.ActivityField;
import com.den.pulse.domain.task.entity.TaskActivity;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskActivityResponse(
        UUID id,
        UUID taskId,
        ActivityField field,
        String oldValue,
        String newValue,
        UUID changedById,
        LocalDateTime createdAt
) {

    public static TaskActivityResponse from(TaskActivity activity) {
        return new TaskActivityResponse(
                activity.getId(),
                activity.getTask().getId(),
                activity.getField(),
                activity.getOldValue(),
                activity.getNewValue(),
                activity.getChangedBy().getId(),
                activity.getCreatedAt()
        );
    }
}
