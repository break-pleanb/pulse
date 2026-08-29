package com.den.pulse.domain.project.service;

import com.den.pulse.domain.project.dto.CreateFolderRequest;
import com.den.pulse.domain.project.dto.FolderResponse;
import com.den.pulse.domain.project.entity.Folder;
import com.den.pulse.domain.project.repository.FolderRepository;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {

    private final EntityManager entityManager;
    private final FolderRepository folderRepository;

    public List<FolderResponse> getMyFolders(UUID userId) {
        return folderRepository.findByOwner_Id(userId).stream()
                .map(FolderResponse::from)
                .toList();
    }

    @Transactional
    public FolderResponse createFolder(UUID userId, CreateFolderRequest request) {
        User owner = entityManager.getReference(User.class, userId);
        Folder folder = new Folder(owner, request.name());
        entityManager.persist(folder);
        return FolderResponse.from(folder);
    }
}
