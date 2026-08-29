package com.den.pulse.domain.project.service;

import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.project.dto.FavoriteToggleResponse;
import com.den.pulse.domain.project.entity.Favorite;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.project.repository.FavoriteRepository;
import com.den.pulse.domain.project.repository.ProjectRepository;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private static final String PROJECT_NOT_FOUND_MESSAGE = "프로젝트를 찾을 수 없습니다.";

    private final EntityManager entityManager;
    private final ProjectRepository projectRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<UUID> getMyFavoriteProjectIds(UUID userId) {
        return favoriteRepository.findProjectIdsByUser_Id(userId);
    }

    @Transactional
    public FavoriteToggleResponse toggleFavorite(UUID userId, String projectKey) {
        Project project = projectRepository.findByKey(projectKey)
                .orElseThrow(() -> new NotFoundException(PROJECT_NOT_FOUND_MESSAGE));
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(project.getId(), userId)) {
            throw new NotFoundException(PROJECT_NOT_FOUND_MESSAGE);
        }

        Optional<Favorite> existing = favoriteRepository.findByUser_IdAndProject_Id(userId, project.getId());
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return new FavoriteToggleResponse(false);
        }

        User user = entityManager.getReference(User.class, userId);
        entityManager.persist(new Favorite(user, project));
        return new FavoriteToggleResponse(true);
    }
}
