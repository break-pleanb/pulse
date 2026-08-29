package com.den.pulse.domain.member.service;

import com.den.pulse.core.exception.ConflictException;
import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.member.dto.InviteMemberRequest;
import com.den.pulse.domain.member.dto.ProjectMemberResponse;
import com.den.pulse.domain.member.dto.UpdateMemberRoleRequest;
import com.den.pulse.domain.member.dto.UpdateMenuPermissionRequest;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.entity.ProjectRole;
import com.den.pulse.domain.member.entity.RoleMenuPermission;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.repository.ProjectRoleRepository;
import com.den.pulse.domain.member.repository.RoleMenuPermissionRepository;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.user.entity.User;
import com.den.pulse.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 멤버 초대·역할 변경·제거, 역할 메뉴권한 수정. 전부 프로젝트 관리자(Role.isAdmin)만 수행 가능하고
 * 관리자가 0명이 되는 상태는 만들 수 없다 (API-SPEC.md 3장, 2026-08-29 확정).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCommandService {

    private static final String USER_NOT_FOUND_MESSAGE = "사용자를 찾을 수 없습니다.";
    private static final String ROLE_NOT_FOUND_MESSAGE = "역할을 찾을 수 없습니다.";
    private static final String MEMBER_NOT_FOUND_MESSAGE = "프로젝트 멤버를 찾을 수 없습니다.";
    private static final String ALREADY_MEMBER_MESSAGE = "이미 프로젝트 멤버입니다.";
    private static final String LAST_ADMIN_MESSAGE = "프로젝트에 관리자가 최소 1명은 있어야 합니다.";

    private final EntityManager entityManager;
    private final ProjectAccessService projectAccessService;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectMemberResponse inviteMember(UUID requesterId, String projectKey, InviteMemberRequest request) {
        ProjectMember requester = projectAccessService.requireAdmin(requesterId, projectKey);
        Project project = requester.getProject();

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));

        if (projectMemberRepository.existsByProject_IdAndUser_Id(project.getId(), user.getId())) {
            throw new ConflictException(ALREADY_MEMBER_MESSAGE);
        }

        ProjectRole role = findRoleInProject(request.roleId(), project.getId());

        ProjectMember member = new ProjectMember(project, user, role, LocalDate.now());
        entityManager.persist(member);
        return ProjectMemberResponse.from(member);
    }

    @Transactional
    public void updateMemberRole(UUID requesterId, String projectKey, UUID targetUserId, UpdateMemberRoleRequest request) {
        ProjectMember requester = projectAccessService.requireAdmin(requesterId, projectKey);
        Project project = requester.getProject();

        ProjectMember target = findMemberWithRole(project.getId(), targetUserId);
        ProjectRole newRole = findRoleInProject(request.roleId(), project.getId());

        if (isLastAdminBeingDemoted(project.getId(), target, newRole)) {
            throw new ConflictException(LAST_ADMIN_MESSAGE);
        }

        target.setRole(newRole);
    }

    @Transactional
    public void removeMember(UUID requesterId, String projectKey, UUID targetUserId) {
        ProjectMember requester = projectAccessService.requireAdmin(requesterId, projectKey);
        Project project = requester.getProject();

        ProjectMember target = findMemberWithRole(project.getId(), targetUserId);

        if (isLastAdmin(project.getId(), target)) {
            throw new ConflictException(LAST_ADMIN_MESSAGE);
        }

        projectMemberRepository.delete(target);
    }

    @Transactional
    public void updateRoleMenuPermission(UUID requesterId, UUID roleId, UpdateMenuPermissionRequest request) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException(ROLE_NOT_FOUND_MESSAGE));
        Project project = role.getProject();
        if (project == null) {
            throw new NotFoundException(ROLE_NOT_FOUND_MESSAGE);
        }
        projectAccessService.requireAdmin(requesterId, project.getId());

        RoleMenuPermission permission = roleMenuPermissionRepository
                .findByRole_IdAndMenuKey(roleId, request.menuKey())
                .orElse(null);
        if (permission != null) {
            permission.setEnabled(request.value());
        } else {
            entityManager.persist(new RoleMenuPermission(role, request.menuKey(), request.value()));
        }
    }

    private ProjectMember findMemberWithRole(UUID projectId, UUID userId) {
        return projectMemberRepository.findByProject_IdAndUser_IdFetchRole(projectId, userId)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND_MESSAGE));
    }

    private ProjectRole findRoleInProject(UUID roleId, UUID projectId) {
        return projectRoleRepository.findById(roleId)
                .filter(role -> role.getProject() != null && role.getProject().getId().equals(projectId))
                .orElseThrow(() -> new NotFoundException(ROLE_NOT_FOUND_MESSAGE));
    }

    private boolean isLastAdmin(UUID projectId, ProjectMember member) {
        return member.getRole().isAdmin()
                && projectMemberRepository.countByProject_IdAndRole_AdminTrue(projectId) <= 1;
    }

    private boolean isLastAdminBeingDemoted(UUID projectId, ProjectMember target, ProjectRole newRole) {
        return !newRole.isAdmin() && isLastAdmin(projectId, target);
    }
}
