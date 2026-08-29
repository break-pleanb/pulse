package com.den.pulse.domain.member.dto;

import com.den.pulse.domain.member.entity.ProjectMember;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID userId,
        UUID projectId,
        UUID roleId,
        LocalDate invitedAt
) {

    public static ProjectMemberResponse from(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getUser().getId(),
                member.getProject().getId(),
                member.getRole().getId(),
                member.getInvitedAt()
        );
    }
}
