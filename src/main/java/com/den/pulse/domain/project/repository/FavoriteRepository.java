package com.den.pulse.domain.project.repository;

import com.den.pulse.domain.project.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByUser_IdAndProject_Id(UUID userId, UUID projectId);

    @Query("select f.project.id from Favorite f where f.user.id = :userId")
    List<UUID> findProjectIdsByUser_Id(@Param("userId") UUID userId);
}
