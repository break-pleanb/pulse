package com.den.pulse.domain.auth.dto;

import com.den.pulse.domain.user.dto.UserResponse;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
