package com.den.pulse.domain.member.service;

import com.den.pulse.core.exception.ForbiddenException;
import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.entity.RoleMenuPermission;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.repository.RoleMenuPermissionRepository;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 프로젝트 하위 리소스 전반에서 공통으로 쓰는 멤버십·메뉴권한·관리자 권한 체크
 * (CLAUDE.md "권한 체크를 컨트롤러마다 중복 구현 (공통화할 것)", API-SPEC.md 0.4절).
 * 멤버가 아니면 404(존재 은닉), 멤버이지만 관리자(Role.isAdmin)나 메뉴 권한이 없으면 403.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectAccessService {

    private static final String PROJECT_NOT_FOUND_MESSAGE = "프로젝트를 찾을 수 없습니다.";
    private static final String ADMIN_ONLY_MESSAGE = "관리자만 할 수 있습니다.";
    private static final String MENU_FORBIDDEN_MESSAGE = "이 메뉴에 대한 접근 권한이 없습니다.";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;

    public Project requireMember(UUID userId, String projectKey) {
        Project project = findProjectByKey(projectKey);
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(project.getId(), userId)) {
            throw new NotFoundException(PROJECT_NOT_FOUND_MESSAGE);
        }
        return project;
    }

    public ProjectMember requireMemberWithRole(UUID userId, String projectKey) {
        Project project = findProjectByKey(projectKey);
        return findMemberWithRole(project.getId(), userId);
    }

    public ProjectMember requireMemberWithRole(UUID userId, UUID projectId) {
        return findMemberWithRole(projectId, userId);
    }

    public ProjectMember requireAdmin(UUID userId, String projectKey) {
        ProjectMember member = requireMemberWithRole(userId, projectKey);
        requireAdminRole(member);
        return member;
    }

    public ProjectMember requireAdmin(UUID userId, UUID projectId) {
        ProjectMember member = findMemberWithRole(projectId, userId);
        requireAdminRole(member);
        return member;
    }

    /** 멤버십 확인 후 역할의 메뉴 권한(업무/간트/메신저)이 꺼져 있으면 403 (API-SPEC.md 0.4절). */
    public ProjectMember requireMenuAccess(UUID userId, String projectKey, MenuKey menuKey) {
        ProjectMember member = requireMemberWithRole(userId, projectKey);
        requireMenuEnabled(member, menuKey);
        return member;
    }

    public ProjectMember requireMenuAccess(UUID userId, UUID projectId, MenuKey menuKey) {
        ProjectMember member = requireMemberWithRole(userId, projectId);
        requireMenuEnabled(member, menuKey);
        return member;
    }

    private Project findProjectByKey(String projectKey) {
        return projectRepository.findByKey(projectKey)
                .orElseThrow(() -> new NotFoundException(PROJECT_NOT_FOUND_MESSAGE));
    }

    private ProjectMember findMemberWithRole(UUID projectId, UUID userId) {
        return projectMemberRepository.findByProject_IdAndUser_IdFetchRole(projectId, userId)
                .orElseThrow(() -> new NotFoundException(PROJECT_NOT_FOUND_MESSAGE));
    }

    private void requireAdminRole(ProjectMember member) {
        if (!member.getRole().isAdmin()) {
            throw new ForbiddenException(ADMIN_ONLY_MESSAGE);
        }
    }

    private void requireMenuEnabled(ProjectMember member, MenuKey menuKey) {
        boolean enabled = roleMenuPermissionRepository.findByRole_IdAndMenuKey(member.getRole().getId(), menuKey)
                .map(RoleMenuPermission::isEnabled)
                .orElse(false);
        if (!enabled) {
            throw new ForbiddenException(MENU_FORBIDDEN_MESSAGE);
        }
    }
}
