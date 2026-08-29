package com.den.pulse.domain.member.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.member.dto.UpdateMenuPermissionRequest;
import com.den.pulse.domain.member.service.MemberCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final MemberCommandService memberCommandService;

    @PatchMapping("/{roleId}/menu-permissions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMenuPermission(@CurrentUser UUID userId, @PathVariable UUID roleId,
                                      @Valid @RequestBody UpdateMenuPermissionRequest request) {
        memberCommandService.updateRoleMenuPermission(userId, roleId, request);
    }
}
