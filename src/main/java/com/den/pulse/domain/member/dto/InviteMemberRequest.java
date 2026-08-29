package com.den.pulse.domain.member.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InviteMemberRequest(
        @NotNull(message = "userId는 필수입니다.")
        UUID userId,

        @NotNull(message = "roleId는 필수입니다.")
        UUID roleId
) {
}
