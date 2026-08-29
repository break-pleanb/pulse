package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    long countByParent_Id(UUID parentId);

    @Query("select t.code from Task t where t.project.id = :projectId")
    List<String> findCodesByProject_Id(@Param("projectId") UUID projectId);

    @Query("""
            select t from Task t
            where t.endDate between :today and :tomorrow
              and t.status <> com.den.pulse.domain.task.entity.TaskStatus.DONE
            """)
    List<Task> findDueSoon(@Param("today") LocalDate today, @Param("tomorrow") LocalDate tomorrow);

    @Query("""
            select t.project.id as projectId, t.status as status, count(t) as cnt
            from Task t
            where t.project.id in :projectIds
              and (t.isPrivate = false
                   or exists (select 1 from TaskAssignee a where a.task = t and a.user.id = :userId)
                   or exists (select 1 from TaskWatcher w where w.task = t and w.user.id = :userId))
            group by t.project.id, t.status
            """)
    List<ProjectStatusCountView> countByProjectAndStatus(@Param("projectIds") Collection<UUID> projectIds,
                                                           @Param("userId") UUID userId);
}
