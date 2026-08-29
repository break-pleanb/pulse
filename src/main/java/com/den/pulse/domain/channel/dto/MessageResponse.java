package com.den.pulse.domain.channel.dto;

import com.den.pulse.domain.channel.entity.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID channelId,
        UUID authorId,
        String body,
        List<UUID> mentionUserIds,
        LocalDateTime createdAt
) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChannel().getId(),
                message.getAuthor().getId(),
                message.getBody(),
                message.getMentionUserIds(),
                message.getCreatedAt()
        );
    }
}
