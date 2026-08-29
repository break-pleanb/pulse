package com.den.pulse.domain.project.dto;

import com.den.pulse.domain.project.entity.Folder;

import java.util.UUID;

public record FolderResponse(UUID id, String name, boolean collapsed) {

    public static FolderResponse from(Folder folder) {
        return new FolderResponse(folder.getId(), folder.getName(), folder.isCollapsed());
    }
}
