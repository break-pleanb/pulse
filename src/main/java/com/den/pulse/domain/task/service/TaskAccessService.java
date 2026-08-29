package com.den.pulse.domain.task.service;

import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.service.ProjectAccessService;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.repository.TaskAssigneeRepository;
import com.den.pulse.domain.task.repository.TaskRepository;
import com.den.pulse.domain.task.repository.TaskWatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * taskId로 진입하는 업무 하위 리소스 전반에서 공통으로 쓰는 접근 권한 체크
 * (CLAUDE.md "권한 체크를 컨트롤러마다 중복 구현 (공통화할 것)").
 * 1) 프로젝트 멤버가 아니면 404, 2) 역할의 tasks 메뉴 권한이 꺼져 있으면 403,
 * 3) isPrivate 업무인데 담당자·참여자가 아니면 404 (API-SPEC.md 0.4절, DEN-DESIGN.md 5.3절).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskAccessService {

    private static final String TASK_NOT_FOUND_MESSAGE = "업무를 찾을 수 없습니다.";

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskWatcherRepository taskWatcherRepository;
    private final ProjectAccessService projectAccessService;

    public Task requireVisibleTask(UUID userId, UUID taskId) {
        Task task = findTask(taskId);
        projectAccessService.requireMenuAccess(userId, task.getProject().getId(), MenuKey.TASKS);
        requireVisible(userId, task);
        return task;
    }

    public void requireVisible(UUID userId, Task task) {
        if (task.isPrivate() && !isAssigneeOrWatcher(userId, task.getId())) {
            throw new NotFoundException(TASK_NOT_FOUND_MESSAGE);
        }
    }

    private Task findTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND_MESSAGE));
    }

    private boolean isAssigneeOrWatcher(UUID userId, UUID taskId) {
        return taskAssigneeRepository.existsByTask_IdAndUser_Id(taskId, userId)
                || taskWatcherRepository.existsByTask_IdAndUser_Id(taskId, userId);
    }
}
