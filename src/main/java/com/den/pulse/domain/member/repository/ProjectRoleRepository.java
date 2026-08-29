package com.den.pulse.domain.member.repository;

import com.den.pulse.domain.member.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, UUID> {

    List<ProjectRole> findByProject_Id(UUID projectId);
}
