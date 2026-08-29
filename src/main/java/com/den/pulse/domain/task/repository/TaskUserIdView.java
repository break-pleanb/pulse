package com.den.pulse.domain.task.repository;

import java.util.UUID;

/**
 * TaskAssignee/TaskWatcher의 User 엔티티를 로딩하지 않고 (taskId, userId) 쌍만 배치 조회하기 위한 프로젝션.
 */
public interface TaskUserIdView {

    UUID getTaskId();

    UUID getUserId();
}
