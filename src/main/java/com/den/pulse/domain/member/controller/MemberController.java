package com.den.pulse.domain.member.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.member.dto.InviteMemberRequest;
import com.den.pulse.domain.member.dto.MenuPermissionsResponse;
import com.den.pulse.domain.member.dto.ProjectMemberResponse;
import com.den.pulse.domain.member.dto.RoleResponse;
import com.den.pulse.domain.member.dto.UpdateMemberRoleRequest;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.service.MemberCommandService;
import com.den.pulse.domain.member.service.MemberQueryService;
import com.den.pulse.domain.member.service.ProjectAccessService;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectKey}")
@RequiredArgsConstructor
public class MemberController {

    private final ProjectAccessService projectAccessService;
    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    @GetMapping("/users")
    public List<UserResponse> getProjectUsers(@CurrentUser UUID userId, @PathVariable String projectKey) {
        Project project = projectAccessService.requireMember(userId, projectKey);
        return memberQueryService.getProjectUsers(project.getId());
    }

    @GetMapping("/roles")
    public List<RoleResponse> getProjectRoles(@CurrentUser UUID userId, @PathVariable String projectKey) {
        Project project = projectAccessService.requireMember(userId, projectKey);
        return memberQueryService.getProjectRoles(project.getId());
    }

    @GetMapping("/members")
    public List<ProjectMemberResponse> getProjectMembers(@CurrentUser UUID userId, @PathVariable String projectKey) {
        Project project = projectAccessService.requireMember(userId, projectKey);
        return memberQueryService.getProjectMembers(project.getId());
    }

    @GetMapping("/members/roles")
    public Map<UUID, RoleResponse> getMemberRoles(@CurrentUser UUID userId, @PathVariable String projectKey) {
        Project project = projectAccessService.requireMember(userId, projectKey);
        return memberQueryService.getMemberRoleMap(project.getId());
    }

    @GetMapping("/menu-permissions")
    public MenuPermissionsResponse getMenuPermissions(@CurrentUser UUID userId, @PathVariable String projectKey) {
        ProjectMember member = projectAccessService.requireMemberWithRole(userId, projectKey);
        return memberQueryService.getMenuPermissionsForRole(member.getRole().getId());
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse inviteMember(@CurrentUser UUID userId, @PathVariable String projectKey,
                                               @Valid @RequestBody InviteMemberRequest request) {
        return memberCommandService.inviteMember(userId, projectKey, request);
    }

    @PatchMapping("/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMemberRole(@CurrentUser UUID requesterId, @PathVariable String projectKey,
                                  @PathVariable("userId") UUID targetUserId,
                                  @Valid @RequestBody UpdateMemberRoleRequest request) {
        memberCommandService.updateMemberRole(requesterId, projectKey, targetUserId, request);
    }

    @DeleteMapping("/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@CurrentUser UUID requesterId, @PathVariable String projectKey,
                              @PathVariable("userId") UUID targetUserId) {
        memberCommandService.removeMember(requesterId, projectKey, targetUserId);
    }
}
