package com.den.pulse.domain.task.service;

import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.service.ProjectAccessService;
import com.den.pulse.domain.notification.entity.NotificationType;
import com.den.pulse.domain.notification.service.NotificationService;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.task.dto.CreateSubtaskRequest;
import com.den.pulse.domain.task.dto.CreateTaskRequest;
import com.den.pulse.domain.task.dto.TaskResponse;
import com.den.pulse.domain.task.dto.UpdateAssigneesRequest;
import com.den.pulse.domain.task.dto.UpdateDependenciesRequest;
import com.den.pulse.domain.task.dto.UpdateTaskRequest;
import com.den.pulse.domain.task.dto.UpdateTaskStatusRequest;
import com.den.pulse.domain.task.entity.Tag;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.entity.TaskAssignee;
import com.den.pulse.domain.task.entity.TaskDependency;
import com.den.pulse.domain.task.entity.TaskPriority;
import com.den.pulse.domain.task.entity.TaskStatus;
import com.den.pulse.domain.task.entity.TaskTag;
import com.den.pulse.domain.task.repository.TagRepository;
import com.den.pulse.domain.task.repository.TaskAssigneeRepository;
import com.den.pulse.domain.task.repository.TaskDependencyIdView;
import com.den.pulse.domain.task.repository.TaskDependencyRepository;
import com.den.pulse.domain.task.repository.TaskRepository;
import com.den.pulse.domain.task.repository.TaskWatcherRepository;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCommandService {

    private final EntityManager entityManager;
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskWatcherRepository taskWatcherRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TagRepository tagRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final TaskAccessService taskAccessService;
    private final TaskResponseAssembler assembler;
    private final NotificationService notificationService;

    /**
     * 최상위 업무 생성 (API-SPEC.md 4장, 2026-08-29 추가 — 기존엔 하위업무 생성만 있어 최상위 업무를
     * 만들 방법이 없었다). title만 필수이고 나머지는 기본값을 쓴다.
     */
    @Transactional
    public TaskResponse createTask(UUID userId, String projectKey, CreateTaskRequest request) {
        ProjectMember member = projectAccessService.requireMenuAccess(userId, projectKey, MenuKey.TASKS);
        Project project = member.getProject();

        LocalDate startDate = request.startDate() != null ? request.startDate()
                : (request.endDate() != null ? request.endDate() : LocalDate.now());
        LocalDate endDate = request.endDate() != null ? request.endDate() : startDate;
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        String code = generateTaskCode(project, project.getKey());
        Task task = new Task(project, code, request.title(),
                request.status() != null ? request.status() : TaskStatus.TODO,
                request.priority() != null ? request.priority() : TaskPriority.MEDIUM,
                null, startDate, endDate, 0,
                request.isPrivate() != null && request.isPrivate());
        entityManager.persist(task);

        Set<UUID> assigneeIds = request.assigneeIds() != null ? new LinkedHashSet<>(request.assigneeIds()) : Set.of();
        validateProjectMembers(project.getId(), assigneeIds);
        for (UUID uid : assigneeIds) {
            User user = entityManager.getReference(User.class, uid);
            entityManager.persist(new TaskAssignee(task, user));
            notificationService.notify(user, NotificationType.TASK_ASSIGNED,
                    "새 업무에 담당자로 지정되었습니다", task.getTitle(), project, task, null);
        }

        Set<UUID> tagIds = request.tagIds() != null ? new LinkedHashSet<>(request.tagIds()) : Set.of();
        validateProjectTags(project.getId(), tagIds);
        for (UUID tagId : tagIds) {
            Tag tag = entityManager.getReference(Tag.class, tagId);
            entityManager.persist(new TaskTag(task, tag));
        }

        return assembler.toResponse(task);
    }

    @Transactional
    public TaskResponse createSubtask(UUID userId, UUID parentId, CreateSubtaskRequest request) {
        Task parent = taskAccessService.requireVisibleTask(userId, parentId);
        String code = generateTaskCode(parent.getProject(), parent.getCode().split("-")[0]);

        Task subtask = new Task(parent.getProject(), code, request.title(), TaskStatus.TODO,
                TaskPriority.MEDIUM, parent, parent.getStartDate(), parent.getEndDate(), 0, false);
        entityManager.persist(subtask);

        return assembler.toResponse(subtask);
    }

    @Transactional
    public TaskResponse updateStatus(UUID userId, UUID taskId, UpdateTaskStatusRequest request) {
        Task task = taskAccessService.requireVisibleTask(userId, taskId);

        task.setStatus(request.status());
        if (request.status() == TaskStatus.DONE) {
            task.setProgress(100);
        }

        notifyStatusChanged(userId, task);
        return assembler.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID userId, UUID taskId, UpdateTaskRequest request) {
        Task task = taskAccessService.requireVisibleTask(userId, taskId);

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new IllegalArgumentException("제목을 입력해 주세요.");
            }
            task.setTitle(request.title());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        LocalDate newStart = request.startDate() != null ? request.startDate() : task.getStartDate();
        LocalDate newEnd = request.endDate() != null ? request.endDate() : task.getEndDate();
        if (newStart.isAfter(newEnd)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }
        if (request.startDate() != null) {
            task.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            task.setEndDate(request.endDate());
        }
        if (request.progress() != null) {
            task.setProgress(request.progress());
        }
        if (request.isPrivate() != null) {
            task.setPrivate(request.isPrivate());
        }

        return assembler.toResponse(task);
    }

    @Transactional
    public TaskResponse updateAssignees(UUID userId, UUID taskId, UpdateAssigneesRequest request) {
        Task task = taskAccessService.requireVisibleTask(userId, taskId);
        Set<UUID> newIds = new LinkedHashSet<>(request.assigneeIds());
        validateProjectMembers(task.getProject().getId(), newIds);

        Set<UUID> currentIds = new HashSet<>(taskAssigneeRepository.findUserIdsByTask_Id(taskId));
        Set<UUID> added = new HashSet<>(newIds);
        added.removeAll(currentIds);

        taskAssigneeRepository.deleteByTask_Id(taskId);
        entityManager.flush();
        for (UUID uid : newIds) {
            User user = entityManager.getReference(User.class, uid);
            entityManager.persist(new TaskAssignee(task, user));
        }

        for (UUID uid : added) {
            User user = entityManager.getReference(User.class, uid);
            notificationService.notify(user, NotificationType.TASK_ASSIGNED,
                    "새 업무에 담당자로 지정되었습니다", task.getTitle(), task.getProject(), task, null);
        }

        return assembler.toResponse(task);
    }

    @Transactional
    public TaskResponse updateDependencies(UUID userId, UUID taskId, UpdateDependenciesRequest request) {
        Task task = taskAccessService.requireVisibleTask(userId, taskId);
        Set<UUID> depIds = new LinkedHashSet<>(request.dependencyIds());

        if (depIds.contains(taskId)) {
            throw new IllegalArgumentException("자기 자신을 선행 업무로 지정할 수 없습니다.");
        }
        List<Task> depTasks = taskRepository.findAllById(depIds);
        if (depTasks.size() != depIds.size()
                || depTasks.stream().anyMatch(t -> !t.getProject().getId().equals(task.getProject().getId()))) {
            throw new IllegalArgumentException("같은 프로젝트의 업무만 선행 업무로 지정할 수 있습니다.");
        }
        validateNoCycle(task.getProject().getId(), taskId, depIds);

        taskDependencyRepository.deleteByTask_Id(taskId);
        entityManager.flush();
        for (Task dependsOn : depTasks) {
            entityManager.persist(new TaskDependency(task, dependsOn));
        }

        return assembler.toResponse(task);
    }

    private void notifyStatusChanged(UUID actorId, Task task) {
        Set<UUID> recipientIds = new HashSet<>(taskAssigneeRepository.findUserIdsByTask_Id(task.getId()));
        recipientIds.addAll(taskWatcherRepository.findUserIdsByTask_Id(task.getId()));
        recipientIds.remove(actorId);

        for (UUID uid : recipientIds) {
            User user = entityManager.getReference(User.class, uid);
            notificationService.notify(user, NotificationType.TASK_STATUS_CHANGED,
                    "업무 상태가 변경되었습니다", task.getTitle(), task.getProject(), task, null);
        }
    }

    private void validateProjectMembers(UUID projectId, Set<UUID> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        long matched = projectMemberRepository.countByProject_IdAndUser_IdIn(projectId, userIds);
        if (matched != userIds.size()) {
            throw new IllegalArgumentException("프로젝트 멤버가 아닌 사용자는 담당자로 지정할 수 없습니다.");
        }
    }

    private void validateProjectTags(UUID projectId, Set<UUID> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size() || tags.stream().anyMatch(t -> !t.getProject().getId().equals(projectId))) {
            throw new IllegalArgumentException("프로젝트에 속하지 않은 태그입니다.");
        }
    }

    private void validateNoCycle(UUID projectId, UUID taskId, Set<UUID> newDependsOnIds) {
        Map<UUID, List<UUID>> graph = taskDependencyRepository.findEdgesByProject_Id(projectId).stream()
                .collect(Collectors.groupingBy(
                        TaskDependencyIdView::getTaskId,
                        Collectors.mapping(TaskDependencyIdView::getDependsOnId, Collectors.toList())
                ));

        for (UUID start : newDependsOnIds) {
            if (canReach(graph, start, taskId, new HashSet<>())) {
                throw new IllegalArgumentException("선행 업무 지정이 순환 참조를 만듭니다.");
            }
        }
    }

    private boolean canReach(Map<UUID, List<UUID>> graph, UUID from, UUID target, Set<UUID> visited) {
        if (from.equals(target)) {
            return true;
        }
        if (!visited.add(from)) {
            return false;
        }
        for (UUID next : graph.getOrDefault(from, List.of())) {
            if (canReach(graph, next, target, visited)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 주어진 접두어에 그 프로젝트 내 최대 순번 + 1을 붙인다 (API-SPEC.md 4장).
     * 최상위 업무는 프로젝트 접두어(project.key)를, 하위업무는 부모 코드의 접두어(code.split('-')[0])를 쓴다.
     */
    private String generateTaskCode(Project project, String prefix) {
        int maxSeq = 0;
        for (String code : taskRepository.findCodesByProject_Id(project.getId())) {
            int idx = code.lastIndexOf('-');
            if (idx < 0) {
                continue;
            }
            try {
                maxSeq = Math.max(maxSeq, Integer.parseInt(code.substring(idx + 1)));
            } catch (NumberFormatException ignored) {
                // 접두어-순번 형식이 아닌 코드는 순번 계산에서 제외
            }
        }
        return prefix + "-" + (maxSeq + 1);
    }
}
