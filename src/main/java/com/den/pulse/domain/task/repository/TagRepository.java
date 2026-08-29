package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByProject_Id(UUID projectId);
}
