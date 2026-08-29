package com.den.pulse.domain.member.service;

import com.den.pulse.domain.member.dto.RoleResponse;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.entity.ProjectRole;
import com.den.pulse.domain.member.entity.RoleMenuPermission;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.repository.RoleMenuPermissionRepository;
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
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;

    public Map<UUID, RoleResponse> getMyProjectRoles(UUID userId) {
        List<ProjectMember> members = projectMemberRepository.findAllByUserIdFetchRoleAndProject(userId);

        List<UUID> roleIds = members.stream()
                .map(m -> m.getRole().getId())
                .distinct()
                .toList();

        Map<UUID, Map<MenuKey, Boolean>> permissionsByRole = roleMenuPermissionRepository.findByRole_IdIn(roleIds).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getRole().getId(),
                        Collectors.toMap(RoleMenuPermission::getMenuKey, RoleMenuPermission::isEnabled)
                ));

        Map<UUID, RoleResponse> result = new LinkedHashMap<>();
        for (ProjectMember member : members) {
            ProjectRole role = member.getRole();
            Map<MenuKey, Boolean> permissions = permissionsByRole.getOrDefault(role.getId(), Map.of());
            result.put(member.getProject().getId(), RoleResponse.from(role, permissions));
        }
        return result;
    }
}
