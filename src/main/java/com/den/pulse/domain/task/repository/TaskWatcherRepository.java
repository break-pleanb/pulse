package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.TaskWatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TaskWatcherRepository extends JpaRepository<TaskWatcher, UUID> {

    boolean existsByTask_IdAndUser_Id(UUID taskId, UUID userId);

    @Query("select w.user.id from TaskWatcher w where w.task.id = :taskId")
    List<UUID> findUserIdsByTask_Id(@Param("taskId") UUID taskId);

    @Query("select w.task.id as taskId, w.user.id as userId from TaskWatcher w where w.task.id in :taskIds")
    List<TaskUserIdView> findByTaskIdIn(@Param("taskIds") Collection<UUID> taskIds);
}
