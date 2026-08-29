package com.den.pulse.domain.channel.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDmRequest(
        @NotNull(message = "targetUserId는 필수입니다.")
        UUID targetUserId
) {
}
