package com.den.pulse.domain.member.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateMemberRoleRequest(
        @NotNull(message = "roleId는 필수입니다.")
        UUID roleId
) {
}
