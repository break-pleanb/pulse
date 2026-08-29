package com.den.pulse.domain.task.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.task.dto.ProjectTaskStatsResponse;
import com.den.pulse.domain.task.service.TaskQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MeTaskController {

    private final TaskQueryService taskQueryService;

    @GetMapping("/api/me/project-stats")
    public List<ProjectTaskStatsResponse> myProjectStats(@CurrentUser UUID userId) {
        return taskQueryService.getMyProjectStats(userId);
    }

    @GetMapping("/api/me/task-count")
    public long myTaskCount(@CurrentUser UUID userId) {
        return taskQueryService.getMyOpenTaskCount(userId);
    }
}
