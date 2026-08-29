package com.den.pulse.domain.project.repository;

import com.den.pulse.domain.project.entity.ProjectPlacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectPlacementRepository extends JpaRepository<ProjectPlacement, UUID> {

    Optional<ProjectPlacement> findByUser_IdAndProject_Id(UUID userId, UUID projectId);

    @Query("select pp from ProjectPlacement pp left join fetch pp.folder where pp.user.id = :userId and pp.project.id in :projectIds")
    List<ProjectPlacement> findAllByUserIdAndProjectIdInFetchFolder(@Param("userId") UUID userId,
                                                                     @Param("projectIds") Collection<UUID> projectIds);
}
