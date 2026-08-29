package com.den.pulse.domain.member.repository;

import com.den.pulse.domain.member.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);

    long countByProject_IdAndRole_AdminTrue(UUID projectId);

    List<ProjectMember> findByProject_Id(UUID projectId);

    @Query("select pm from ProjectMember pm join fetch pm.project where pm.user.id = :userId")
    List<ProjectMember> findAllByUserIdFetchProject(@Param("userId") UUID userId);

    @Query("select pm from ProjectMember pm join fetch pm.role join fetch pm.project where pm.user.id = :userId")
    List<ProjectMember> findAllByUserIdFetchRoleAndProject(@Param("userId") UUID userId);

    @Query("select pm from ProjectMember pm join fetch pm.role where pm.project.id = :projectId")
    List<ProjectMember> findByProject_IdFetchRole(@Param("projectId") UUID projectId);

    @Query("select pm from ProjectMember pm join fetch pm.user where pm.project.id = :projectId")
    List<ProjectMember> findByProject_IdFetchUser(@Param("projectId") UUID projectId);

    @Query("select pm from ProjectMember pm join fetch pm.role where pm.project.id = :projectId and pm.user.id = :userId")
    Optional<ProjectMember> findByProject_IdAndUser_IdFetchRole(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    @Query("select pm.project.id as projectId, pm.user.id as userId from ProjectMember pm where pm.project.id in :projectIds")
    List<ProjectMemberIdsView> findMemberIdsByProjectIdIn(@Param("projectIds") Collection<UUID> projectIds);
}
