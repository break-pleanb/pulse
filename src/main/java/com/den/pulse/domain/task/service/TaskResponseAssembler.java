package com.den.pulse.domain.task.service;

import com.den.pulse.domain.task.dto.TaskResponse;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.repository.TaskAssigneeRepository;
import com.den.pulse.domain.task.repository.TaskDependencyIdView;
import com.den.pulse.domain.task.repository.TaskDependencyRepository;
import com.den.pulse.domain.task.repository.TaskTagIdView;
import com.den.pulse.domain.task.repository.TaskTagRepository;
import com.den.pulse.domain.task.repository.TaskUserIdView;
import com.den.pulse.domain.task.repository.TaskWatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Task 엔티티를 TaskResponse로 변환한다. assigneeIds/watcherIds/dependencyIds/tagIds는
 * 조인 테이블에서 파생되므로, 목록이든 단건이든 항상 taskId 목록 단위로 배치 조회한다 (N+1 방지, CLAUDE.md 7-4).
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class TaskResponseAssembler {

    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskWatcherRepository taskWatcherRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskTagRepository taskTagRepository;

    TaskResponse toResponse(Task task) {
        return toResponses(List.of(task)).get(task.getId());
    }

    Map<UUID, TaskResponse> toResponses(List<Task> tasks) {
        List<UUID> taskIds = tasks.stream().map(Task::getId).toList();

        Map<UUID, List<UUID>> assigneeIds = groupUserIds(taskAssigneeRepository.findByTaskIdIn(taskIds));
        Map<UUID, List<UUID>> watcherIds = groupUserIds(taskWatcherRepository.findByTaskIdIn(taskIds));
        Map<UUID, List<UUID>> dependencyIds = taskDependencyRepository.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(
                        TaskDependencyIdView::getTaskId,
                        Collectors.mapping(TaskDependencyIdView::getDependsOnId, Collectors.toList())
                ));
        Map<UUID, List<UUID>> tagIds = taskTagRepository.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(
                        TaskTagIdView::getTaskId,
                        Collectors.mapping(TaskTagIdView::getTagId, Collectors.toList())
                ));

        return tasks.stream().collect(Collectors.toMap(Task::getId, task -> TaskResponse.from(
                task,
                assigneeIds.getOrDefault(task.getId(), List.of()),
                watcherIds.getOrDefault(task.getId(), List.of()),
                dependencyIds.getOrDefault(task.getId(), List.of()),
                tagIds.getOrDefault(task.getId(), List.of())
        )));
    }

    private Map<UUID, List<UUID>> groupUserIds(List<TaskUserIdView> rows) {
        return rows.stream().collect(Collectors.groupingBy(
                TaskUserIdView::getTaskId,
                Collectors.mapping(TaskUserIdView::getUserId, Collectors.toList())
        ));
    }
}
