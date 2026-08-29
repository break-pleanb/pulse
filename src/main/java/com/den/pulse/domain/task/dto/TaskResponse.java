package com.den.pulse.domain.task.dto;

import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.entity.TaskPriority;
import com.den.pulse.domain.task.entity.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String code,
        UUID projectId,
        String title,
        TaskStatus status,
        TaskPriority priority,
        List<UUID> assigneeIds,
        List<UUID> watcherIds,
        UUID parentId,
        List<UUID> dependencyIds,
        List<UUID> tagIds,
        LocalDate startDate,
        LocalDate endDate,
        int progress,
        boolean isPrivate,
        int commentCount
) {

    public static TaskResponse from(Task task, List<UUID> assigneeIds, List<UUID> watcherIds,
                                     List<UUID> dependencyIds, List<UUID> tagIds) {
        return new TaskResponse(
                task.getId(),
                task.getCode(),
                task.getProject().getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                assigneeIds,
                watcherIds,
                task.getParent() != null ? task.getParent().getId() : null,
                dependencyIds,
                tagIds,
                task.getStartDate(),
                task.getEndDate(),
                task.getProgress(),
                task.isPrivate(),
                task.getCommentCount()
        );
    }
}
