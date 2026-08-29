package com.den.pulse.domain.channel.dto;

import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.channel.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        UUID projectId,
        String name,
        ChannelType type,
        List<UUID> memberIds,
        int unreadCount
) {

    public static ChannelResponse from(Channel channel, List<UUID> memberIds, int unreadCount) {
        return new ChannelResponse(
                channel.getId(),
                channel.getProject().getId(),
                channel.getName(),
                channel.getType(),
                memberIds,
                unreadCount
        );
    }
}
