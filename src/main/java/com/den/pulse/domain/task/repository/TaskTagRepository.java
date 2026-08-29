package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TaskTagRepository extends JpaRepository<TaskTag, UUID> {

    @Query("select tt.task.id as taskId, tt.tag.id as tagId from TaskTag tt where tt.task.id in :taskIds")
    List<TaskTagIdView> findByTaskIdIn(@Param("taskIds") Collection<UUID> taskIds);
}
