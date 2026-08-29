package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {

    List<TaskActivity> findByTask_IdOrderByCreatedAtAsc(UUID taskId);
}
