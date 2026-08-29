package com.den.pulse.domain.project.repository;

import com.den.pulse.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByKey(String key);

    boolean existsByKey(String key);
}
