package com.den.pulse.domain.task.service;

import com.den.pulse.core.response.PageResponse;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.service.ProjectAccessService;
import com.den.pulse.domain.task.dto.ProjectTaskStatsResponse;
import com.den.pulse.domain.task.dto.TagResponse;
import com.den.pulse.domain.task.dto.TaskResponse;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.entity.TaskPriority;
import com.den.pulse.domain.task.entity.TaskStatus;
import com.den.pulse.domain.task.repository.ProjectStatusCountView;
import com.den.pulse.domain.task.repository.TagRepository;
import com.den.pulse.domain.task.repository.TaskAssigneeRepository;
import com.den.pulse.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskQueryService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TagRepository tagRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final TaskAccessService taskAccessService;
    private final TaskResponseAssembler assembler;

    public PageResponse<TaskResponse> getTasks(UUID userId, String projectKey, String status, String assignee,
                                                String priority, String tag, String q, int page, int size) {
        ProjectMember member = projectAccessService.requireMenuAccess(userId, projectKey, MenuKey.TASKS);
        UUID projectId = member.getProject().getId();

        Specification<Task> spec = Specification.where(TaskSpecifications.inProject(projectId))
                .and(TaskSpecifications.visibleTo(userId));

        List<TaskStatus> statuses = parseStatuses(status);
        if (!statuses.isEmpty()) {
            spec = spec.and(TaskSpecifications.statusIn(statuses));
        }
        List<TaskPriority> priorities = parsePriorities(priority);
        if (!priorities.isEmpty()) {
            spec = spec.and(TaskSpecifications.priorityIn(priorities));
        }
        List<UUID> assigneeIds = parseUuids(assignee);
        if (!assigneeIds.isEmpty()) {
            spec = spec.and(TaskSpecifications.hasAnyAssignee(assigneeIds));
        }
        List<UUID> tagIds = parseUuids(tag);
        if (!tagIds.isEmpty()) {
            spec = spec.and(TaskSpecifications.hasAnyTag(tagIds));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(TaskSpecifications.titleContains(q));
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Task> result = taskRepository.findAll(spec, pageable);

        Map<UUID, TaskResponse> responses = assembler.toResponses(result.getContent());
        List<TaskResponse> items = result.getContent().stream().map(t -> responses.get(t.getId())).toList();

        return new PageResponse<>(items, result.getTotalElements(), page, size);
    }

    public TaskResponse getTask(UUID userId, UUID taskId) {
        Task task = taskAccessService.requireVisibleTask(userId, taskId);
        return assembler.toResponse(task);
    }

    public long getSubtaskCount(UUID userId, UUID taskId) {
        Task task = taskAccessService.requireVisibleTask(userId, taskId);
        return taskRepository.countByParent_Id(task.getId());
    }

    public List<TagResponse> getTags(UUID userId, String projectKey) {
        ProjectMember member = projectAccessService.requireMenuAccess(userId, projectKey, MenuKey.TASKS);
        return tagRepository.findByProject_Id(member.getProject().getId()).stream()
                .map(TagResponse::from)
                .toList();
    }

    public List<ProjectTaskStatsResponse> getMyProjectStats(UUID userId) {
        List<UUID> projectIds = projectMemberRepository.findAllByUserIdFetchProject(userId).stream()
                .map(pm -> pm.getProject().getId())
                .toList();
        if (projectIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Map<TaskStatus, Long>> countsByProject = new LinkedHashMap<>();
        for (UUID projectId : projectIds) {
            countsByProject.put(projectId, new LinkedHashMap<>());
        }
        for (ProjectStatusCountView row : taskRepository.countByProjectAndStatus(projectIds, userId)) {
            countsByProject.get(row.getProjectId()).put(row.getStatus(), row.getCnt());
        }

        return projectIds.stream().map(projectId -> {
            Map<TaskStatus, Long> counts = countsByProject.get(projectId);
            long todo = counts.getOrDefault(TaskStatus.TODO, 0L);
            long progress = counts.getOrDefault(TaskStatus.PROGRESS, 0L);
            long review = counts.getOrDefault(TaskStatus.REVIEW, 0L);
            long done = counts.getOrDefault(TaskStatus.DONE, 0L);
            return new ProjectTaskStatsResponse(projectId, todo + progress + review + done, todo, progress, review, done);
        }).toList();
    }

    public long getMyOpenTaskCount(UUID userId) {
        return taskAssigneeRepository.countOpenAssignedTasks(userId);
    }

    private List<TaskStatus> parseStatuses(String raw) {
        return parseList(raw, v -> {
            try {
                return TaskStatus.valueOf(v.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("올바르지 않은 status 값입니다: " + v);
            }
        });
    }

    private List<TaskPriority> parsePriorities(String raw) {
        return parseList(raw, v -> {
            try {
                return TaskPriority.valueOf(v.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("올바르지 않은 priority 값입니다: " + v);
            }
        });
    }

    private List<UUID> parseUuids(String raw) {
        return parseList(raw, v -> {
            try {
                return UUID.fromString(v);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("올바르지 않은 id 값입니다: " + v);
            }
        });
    }

    private <T> List<T> parseList(String raw, java.util.function.Function<String, T> mapper) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .map(mapper)
                .toList();
    }
}
