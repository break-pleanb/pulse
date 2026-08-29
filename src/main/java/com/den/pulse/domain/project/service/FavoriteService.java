package com.den.pulse.domain.project.service;

import com.den.pulse.domain.member.service.ProjectAccessService;
import com.den.pulse.domain.project.dto.FavoriteToggleResponse;
import com.den.pulse.domain.project.entity.Favorite;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.project.repository.FavoriteRepository;
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

    private final EntityManager entityManager;
    private final FavoriteRepository favoriteRepository;
    private final ProjectAccessService projectAccessService;

    public List<UUID> getMyFavoriteProjectIds(UUID userId) {
        return favoriteRepository.findProjectIdsByUser_Id(userId);
    }

    @Transactional
    public FavoriteToggleResponse toggleFavorite(UUID userId, String projectKey) {
        Project project = projectAccessService.requireMember(userId, projectKey);

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
