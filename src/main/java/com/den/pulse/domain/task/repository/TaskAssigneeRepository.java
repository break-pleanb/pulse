package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.TaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, UUID> {

    boolean existsByTask_IdAndUser_Id(UUID taskId, UUID userId);

    void deleteByTask_Id(UUID taskId);

    @Query("select a.user.id from TaskAssignee a where a.task.id = :taskId")
    List<UUID> findUserIdsByTask_Id(@Param("taskId") UUID taskId);

    @Query("select a.task.id as taskId, a.user.id as userId from TaskAssignee a where a.task.id in :taskIds")
    List<TaskUserIdView> findByTaskIdIn(@Param("taskIds") Collection<UUID> taskIds);

    @Query("select count(a) from TaskAssignee a where a.user.id = :userId and a.task.status <> com.den.pulse.domain.task.entity.TaskStatus.DONE")
    long countOpenAssignedTasks(@Param("userId") UUID userId);
}
