package com.den.pulse.domain.project.service;

import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.channel.entity.ChannelMember;
import com.den.pulse.domain.channel.entity.ChannelType;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.entity.ProjectRole;
import com.den.pulse.domain.member.repository.ProjectMemberIdsView;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.service.ProjectAccessService;
import com.den.pulse.domain.member.service.ProjectRoleService;
import com.den.pulse.domain.project.dto.CreateProjectRequest;
import com.den.pulse.domain.project.dto.PlacementRequest;
import com.den.pulse.domain.project.dto.ProjectResponse;
import com.den.pulse.domain.project.entity.Folder;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.project.entity.ProjectPlacement;
import com.den.pulse.domain.project.repository.FolderRepository;
import com.den.pulse.domain.project.repository.ProjectPlacementRepository;
import com.den.pulse.domain.project.repository.ProjectRepository;
import com.den.pulse.domain.task.repository.TaskRepository;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private static final String FOLDER_NOT_FOUND_MESSAGE = "폴더를 찾을 수 없습니다.";

    /** 프로젝트 생성 시 자동으로 함께 만드는 기본 그룹 채널 이름 (API-SPEC.md 2장, 8단계에서 추가). */
    private static final String DEFAULT_CHANNEL_NAME = "일반";

    /** 프로젝트 생성 시 순환 할당하는 마크 색상 팔레트 (API-SPEC.md 2장 "팔레트에서 순환 할당"). */
    private static final List<String> COLOR_PALETTE = List.of(
            "#6366f1", "#f59e0b", "#10b981", "#ef4444",
            "#3b82f6", "#a855f7", "#ec4899", "#14b8a6"
    );

    private final EntityManager entityManager;
    private final ProjectRepository projectRepository;
    private final ProjectPlacementRepository projectPlacementRepository;
    private final FolderRepository folderRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final ProjectRoleService projectRoleService;
    private final TaskRepository taskRepository;

    public List<ProjectResponse> getMyProjects(UUID userId) {
        List<ProjectMember> members = projectMemberRepository.findAllByUserIdFetchProject(userId);
        List<Project> projects = members.stream().map(ProjectMember::getProject).toList();
        List<UUID> projectIds = projects.stream().map(Project::getId).toList();

        Map<UUID, List<UUID>> memberIdsByProject = groupMemberIds(projectIds);
        Map<UUID, UUID> folderIdByProject = groupFolderIds(userId, projectIds);

        return projects.stream()
                .map(project -> ProjectResponse.from(
                        project,
                        memberIdsByProject.getOrDefault(project.getId(), List.of()),
                        folderIdByProject.get(project.getId())
                ))
                .toList();
    }

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        User user = entityManager.getReference(User.class, userId);

        Folder folder = resolveOwnedFolder(userId, request.folderId());

        Project project = new Project(generateProjectKey(request.name()), request.name(), "", nextColor());
        entityManager.persist(project);

        ProjectRole adminRole = projectRoleService.createDefaultRoles(project);
        entityManager.persist(new ProjectMember(project, user, adminRole, LocalDate.now()));

        Channel defaultChannel = new Channel(project, DEFAULT_CHANNEL_NAME, ChannelType.GROUP);
        entityManager.persist(defaultChannel);
        entityManager.persist(new ChannelMember(defaultChannel, user));

        if (folder != null) {
            entityManager.persist(new ProjectPlacement(user, project, folder));
        }

        UUID folderId = folder != null ? folder.getId() : null;
        return ProjectResponse.from(project, List.of(userId), folderId);
    }

    public ProjectResponse getProjectByKey(UUID userId, String key) {
        Project project = projectAccessService.requireMember(userId, key);
        List<UUID> memberIds = groupMemberIds(List.of(project.getId())).getOrDefault(project.getId(), List.of());
        UUID folderId = groupFolderIds(userId, List.of(project.getId())).get(project.getId());
        return ProjectResponse.from(project, memberIds, folderId);
    }

    @Transactional
    public ProjectResponse updatePlacement(UUID userId, String key, PlacementRequest request) {
        Project project = projectAccessService.requireMember(userId, key);
        Folder folder = resolveOwnedFolder(userId, request.folderId());

        ProjectPlacement placement = projectPlacementRepository
                .findByUser_IdAndProject_Id(userId, project.getId())
                .orElse(null);
        if (placement != null) {
            placement.setFolder(folder);
        } else {
            User user = entityManager.getReference(User.class, userId);
            entityManager.persist(new ProjectPlacement(user, project, folder));
        }

        List<UUID> memberIds = groupMemberIds(List.of(project.getId())).getOrDefault(project.getId(), List.of());
        UUID folderId = folder != null ? folder.getId() : null;
        return ProjectResponse.from(project, memberIds, folderId);
    }

    /**
     * 프로젝트 관리자만 삭제 가능 (사용자 합의, 2026-08-30). 실제 DELETE 대신 deletedAt만 설정하고,
     * 하위 업무도 함께 소프트 삭제한다(cascade — 프로젝트 삭제 시 하위 업무까지 삭제 처리하기로 합의).
     */
    @Transactional
    public void deleteProject(UUID userId, String key) {
        ProjectMember member = projectAccessService.requireAdmin(userId, key);
        Project project = member.getProject();

        LocalDateTime deletedAt = LocalDateTime.now();
        taskRepository.softDeleteAllByProject_Id(project.getId(), deletedAt);
        project.softDelete();
    }

    private Folder resolveOwnedFolder(UUID userId, UUID folderId) {
        if (folderId == null) {
            return null;
        }
        return folderRepository.findById(folderId)
                .filter(f -> f.getOwner().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException(FOLDER_NOT_FOUND_MESSAGE));
    }

    private Map<UUID, List<UUID>> groupMemberIds(List<UUID> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        return projectMemberRepository.findMemberIdsByProjectIdIn(projectIds).stream()
                .collect(Collectors.groupingBy(
                        ProjectMemberIdsView::getProjectId,
                        Collectors.mapping(ProjectMemberIdsView::getUserId, Collectors.toList())
                ));
    }

    private Map<UUID, UUID> groupFolderIds(UUID userId, List<UUID> projectIds) {
        Map<UUID, UUID> folderIdByProject = new HashMap<>();
        if (projectIds.isEmpty()) {
            return folderIdByProject;
        }
        for (ProjectPlacement placement : projectPlacementRepository.findAllByUserIdAndProjectIdInFetchFolder(userId, projectIds)) {
            UUID folderId = placement.getFolder() != null ? placement.getFolder().getId() : null;
            folderIdByProject.put(placement.getProject().getId(), folderId);
        }
        return folderIdByProject;
    }

    /**
     * 프로젝트명에서 ASCII 영숫자만 뽑아 6자 이내 키를 만들고, 충돌하면 순번을 붙인다.
     * 한글 등 ASCII로 변환되지 않는 이름은 "PRJ" 접두어로 대체.
     */
    private String generateProjectKey(String name) {
        String base = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (base.isEmpty()) {
            base = "PRJ";
        } else if (base.length() > 6) {
            base = base.substring(0, 6);
        }

        String candidate = base;
        int suffix = 2;
        while (projectRepository.existsByKey(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String nextColor() {
        long count = projectRepository.count();
        return COLOR_PALETTE.get((int) (count % COLOR_PALETTE.size()));
    }
}
