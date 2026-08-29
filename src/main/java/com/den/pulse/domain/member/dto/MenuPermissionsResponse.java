package com.den.pulse.domain.member.dto;

import com.den.pulse.domain.member.entity.MenuKey;

import java.util.Map;

public record MenuPermissionsResponse(boolean tasks, boolean gantt, boolean messenger) {

    public static MenuPermissionsResponse from(Map<MenuKey, Boolean> permissions) {
        return new MenuPermissionsResponse(
                permissions.getOrDefault(MenuKey.TASKS, false),
                permissions.getOrDefault(MenuKey.GANTT, false),
                permissions.getOrDefault(MenuKey.MESSENGER, false)
        );
    }
}
