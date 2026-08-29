package com.den.pulse.domain.member.service;

import com.den.pulse.domain.member.dto.MenuPermissionsResponse;
import com.den.pulse.domain.member.dto.ProjectMemberResponse;
import com.den.pulse.domain.member.dto.RoleResponse;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.entity.ProjectRole;
import com.den.pulse.domain.member.entity.RoleMenuPermission;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.repository.ProjectRoleRepository;
import com.den.pulse.domain.member.repository.RoleMenuPermissionRepository;
import com.den.pulse.domain.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;

    public Map<UUID, RoleResponse> getMyProjectRoles(UUID userId) {
        List<ProjectMember> members = projectMemberRepository.findAllByUserIdFetchRoleAndProject(userId);
        Map<UUID, Map<MenuKey, Boolean>> permissionsByRole = loadPermissionsByRole(distinctRoleIds(members));

        Map<UUID, RoleResponse> result = new LinkedHashMap<>();
        for (ProjectMember member : members) {
            ProjectRole role = member.getRole();
            result.put(member.getProject().getId(), toRoleResponse(role, permissionsByRole));
        }
        return result;
    }

    public List<RoleResponse> getProjectRoles(UUID projectId) {
        List<ProjectRole> roles = projectRoleRepository.findByProject_Id(projectId);
        List<UUID> roleIds = roles.stream().map(ProjectRole::getId).toList();
        Map<UUID, Map<MenuKey, Boolean>> permissionsByRole = loadPermissionsByRole(roleIds);

        return roles.stream()
                .map(role -> toRoleResponse(role, permissionsByRole))
                .toList();
    }

    public List<ProjectMemberResponse> getProjectMembers(UUID projectId) {
        return projectMemberRepository.findByProject_Id(projectId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    public Map<UUID, RoleResponse> getMemberRoleMap(UUID projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProject_IdFetchRole(projectId);
        Map<UUID, Map<MenuKey, Boolean>> permissionsByRole = loadPermissionsByRole(distinctRoleIds(members));

        Map<UUID, RoleResponse> result = new LinkedHashMap<>();
        for (ProjectMember member : members) {
            result.put(member.getUser().getId(), toRoleResponse(member.getRole(), permissionsByRole));
        }
        return result;
    }

    public List<UserResponse> getProjectUsers(UUID projectId) {
        return projectMemberRepository.findByProject_IdFetchUser(projectId).stream()
                .map(member -> UserResponse.from(member.getUser()))
                .toList();
    }

    public MenuPermissionsResponse getMenuPermissionsForRole(UUID roleId) {
        Map<MenuKey, Boolean> permissions = roleMenuPermissionRepository.findByRole_IdIn(List.of(roleId)).stream()
                .collect(Collectors.toMap(RoleMenuPermission::getMenuKey, RoleMenuPermission::isEnabled));
        return MenuPermissionsResponse.from(permissions);
    }

    private List<UUID> distinctRoleIds(List<ProjectMember> members) {
        return members.stream().map(m -> m.getRole().getId()).distinct().toList();
    }

    private Map<UUID, Map<MenuKey, Boolean>> loadPermissionsByRole(List<UUID> roleIds) {
        return roleMenuPermissionRepository.findByRole_IdIn(roleIds).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getRole().getId(),
                        Collectors.toMap(RoleMenuPermission::getMenuKey, RoleMenuPermission::isEnabled)
                ));
    }

    private RoleResponse toRoleResponse(ProjectRole role, Map<UUID, Map<MenuKey, Boolean>> permissionsByRole) {
        Map<MenuKey, Boolean> permissions = permissionsByRole.getOrDefault(role.getId(), Map.of());
        return RoleResponse.from(role, permissions);
    }
}
