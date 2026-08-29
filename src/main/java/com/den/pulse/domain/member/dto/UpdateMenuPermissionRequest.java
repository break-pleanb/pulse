package com.den.pulse.domain.member.dto;

import com.den.pulse.domain.member.entity.MenuKey;
import jakarta.validation.constraints.NotNull;

public record UpdateMenuPermissionRequest(
        @NotNull(message = "menuKey는 필수입니다.")
        MenuKey menuKey,

        boolean value
) {
}
