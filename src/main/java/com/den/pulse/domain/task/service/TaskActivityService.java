package com.den.pulse.domain.task.service;

import com.den.pulse.domain.task.dto.TaskActivityResponse;
import com.den.pulse.domain.task.entity.ActivityField;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.entity.TaskActivity;
import com.den.pulse.domain.task.repository.TaskActivityRepository;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 업무 필드 변경 이력 조회·기록 (API-SPEC.md 5장). record()는 값이 실제로 바뀐 경우만 남기며,
 * PATCH /tasks/{id}, /status, /assignees 세 곳에서 TaskCommandService가 필드별로 호출한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskActivityService {

    private final EntityManager entityManager;
    private final TaskActivityRepository taskActivityRepository;
    private final TaskAccessService taskAccessService;

    public List<TaskActivityResponse> getActivities(UUID userId, UUID taskId) {
        taskAccessService.requireVisibleTask(userId, taskId);
        return taskActivityRepository.findByTask_IdOrderByCreatedAtAsc(taskId).stream()
                .map(TaskActivityResponse::from)
                .toList();
    }

    @Transactional
    public void record(Task task, ActivityField field, String oldValue, String newValue, UUID changedById) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        User changedBy = entityManager.getReference(User.class, changedById);
        entityManager.persist(new TaskActivity(task, field, oldValue, newValue, changedBy));
    }
}
