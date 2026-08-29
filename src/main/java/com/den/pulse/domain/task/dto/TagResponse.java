package com.den.pulse.domain.task.dto;

import com.den.pulse.domain.task.entity.Tag;

import java.util.UUID;

public record TagResponse(UUID id, UUID projectId, String name) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getProject().getId(), tag.getName());
    }
}
