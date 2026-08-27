package com.den.pulse.domain.user.dto;

import com.den.pulse.domain.user.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String initials,
        String avatarGradient,
        String title
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getInitials(),
                user.getAvatarGradient(),
                user.getTitle()
        );
    }
}
