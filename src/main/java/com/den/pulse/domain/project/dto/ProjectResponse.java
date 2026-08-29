package com.den.pulse.domain.project.dto;

import com.den.pulse.domain.project.entity.Project;

import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String key,
        String name,
        String description,
        String color,
        UUID folderId,
        List<UUID> memberIds
) {

    public static ProjectResponse from(Project project, List<UUID> memberIds, UUID folderId) {
        return new ProjectResponse(
                project.getId(),
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.getColor(),
                folderId,
                memberIds
        );
    }
}
