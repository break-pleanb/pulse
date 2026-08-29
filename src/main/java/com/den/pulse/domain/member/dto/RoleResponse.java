package com.den.pulse.domain.member.dto;

import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectRole;

import java.util.Map;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        UUID projectId,
        String name,
        boolean isAdmin,
        MenuPermissionsResponse menuPermissions
) {

    public static RoleResponse from(ProjectRole role, Map<MenuKey, Boolean> permissions) {
        return new RoleResponse(
                role.getId(),
                role.getProject() != null ? role.getProject().getId() : null,
                role.getName(),
                role.isAdmin(),
                MenuPermissionsResponse.from(permissions)
        );
    }
}
