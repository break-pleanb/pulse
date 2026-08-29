package com.den.pulse.domain.project.repository;

import com.den.pulse.domain.project.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByOwner_Id(UUID ownerId);
}
