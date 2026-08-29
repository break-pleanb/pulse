package com.den.pulse.domain.channel.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record CreateChannelRequest(
        @NotBlank(message = "채널 이름을 입력해 주세요.")
        String name,
        List<UUID> memberIds
) {
}
