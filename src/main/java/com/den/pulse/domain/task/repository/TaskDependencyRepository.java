package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {

    void deleteByTask_Id(UUID taskId);

    /** 업무 활동 이력(dependencies)의 oldValue 기록용 — 변경 전 선행 업무 code 목록. */
    @Query("select d.dependsOn.code from TaskDependency d where d.task.id = :taskId")
    List<String> findDependsOnCodesByTask_Id(@Param("taskId") UUID taskId);

    @Query("select d.task.id as taskId, d.dependsOn.id as dependsOnId from TaskDependency d where d.task.id in :taskIds")
    List<TaskDependencyIdView> findByTaskIdIn(@Param("taskIds") Collection<UUID> taskIds);

    /** 순환 참조 검사용 — 프로젝트 전체 선행 업무 그래프. */
    @Query("select d.task.id as taskId, d.dependsOn.id as dependsOnId from TaskDependency d where d.task.project.id = :projectId")
    List<TaskDependencyIdView> findEdgesByProject_Id(@Param("projectId") UUID projectId);
}
