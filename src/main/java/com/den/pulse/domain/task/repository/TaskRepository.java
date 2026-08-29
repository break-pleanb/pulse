package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    long countByParent_Id(UUID parentId);

    /** 프로젝트 소프트 삭제 시 하위 업무 전체를 함께 소프트 삭제 (cascade). */
    @Modifying
    @Query("update Task t set t.deletedAt = :deletedAt where t.project.id = :projectId and t.deletedAt is null")
    int softDeleteAllByProject_Id(@Param("projectId") UUID projectId, @Param("deletedAt") LocalDateTime deletedAt);

    /** 업무 소프트 삭제 cascade용 — 주어진 부모들의 직계 자식 id 목록. 트리를 한 단계씩 내려가며 반복 호출한다. */
    @Query("select t.id from Task t where t.parent.id in :parentIds")
    List<UUID> findIdsByParent_IdIn(@Param("parentIds") Collection<UUID> parentIds);

    /** 업무 단건 삭제 시 하위 업무(전체 depth)를 함께 소프트 삭제 (cascade). */
    @Modifying
    @Query("update Task t set t.deletedAt = :deletedAt where t.id in :ids and t.deletedAt is null")
    int softDeleteAllByIdIn(@Param("ids") Collection<UUID> ids, @Param("deletedAt") LocalDateTime deletedAt);

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
