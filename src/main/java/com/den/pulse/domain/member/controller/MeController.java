package com.den.pulse.domain.member.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.member.dto.RoleResponse;
import com.den.pulse.domain.member.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/api/me/project-roles")
    public Map<UUID, RoleResponse> myProjectRoles(@CurrentUser UUID userId) {
        return memberQueryService.getMyProjectRoles(userId);
    }
}
