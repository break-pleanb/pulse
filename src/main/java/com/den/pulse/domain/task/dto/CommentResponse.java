package com.den.pulse.domain.task.dto;

import com.den.pulse.domain.task.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID taskId,
        UUID authorId,
        String body,
        List<UUID> mentionUserIds,
        LocalDateTime createdAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getAuthor().getId(),
                comment.getBody(),
                comment.getMentionUserIds(),
                comment.getCreatedAt()
        );
    }
}
