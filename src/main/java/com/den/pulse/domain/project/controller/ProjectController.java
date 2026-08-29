package com.den.pulse.domain.project.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.project.dto.CreateProjectRequest;
import com.den.pulse.domain.project.dto.FavoriteToggleResponse;
import com.den.pulse.domain.project.dto.PlacementRequest;
import com.den.pulse.domain.project.dto.ProjectResponse;
import com.den.pulse.domain.project.service.FavoriteService;
import com.den.pulse.domain.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final FavoriteService favoriteService;

    @GetMapping
    public List<ProjectResponse> myProjects(@CurrentUser UUID userId) {
        return projectService.getMyProjects(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@CurrentUser UUID userId, @Valid @RequestBody CreateProjectRequest request) {
        return projectService.createProject(userId, request);
    }

    @GetMapping("/{projectKey}")
    public ProjectResponse getProject(@CurrentUser UUID userId, @PathVariable String projectKey) {
        return projectService.getProjectByKey(userId, projectKey);
    }

    @PatchMapping("/{projectKey}/placement")
    public ProjectResponse updatePlacement(@CurrentUser UUID userId, @PathVariable String projectKey,
                                            @Valid @RequestBody PlacementRequest request) {
        return projectService.updatePlacement(userId, projectKey, request);
    }

    @PostMapping("/{projectKey}/favorite")
    public FavoriteToggleResponse toggleFavorite(@CurrentUser UUID userId, @PathVariable String projectKey) {
        return favoriteService.toggleFavorite(userId, projectKey);
    }
}
