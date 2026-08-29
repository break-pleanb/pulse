package com.den.pulse.domain.task.controller;

import com.den.pulse.core.response.PageResponse;
import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.task.dto.CreateTaskRequest;
import com.den.pulse.domain.task.dto.TagResponse;
import com.den.pulse.domain.task.dto.TaskResponse;
import com.den.pulse.domain.task.service.TaskCommandService;
import com.den.pulse.domain.task.service.TaskQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectKey}")
@RequiredArgsConstructor
public class ProjectTaskController {

    private final TaskQueryService taskQueryService;
    private final TaskCommandService taskCommandService;

    @GetMapping("/tasks")
    public PageResponse<TaskResponse> getTasks(@CurrentUser UUID userId, @PathVariable String projectKey,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String assignee,
                                                @RequestParam(required = false) String priority,
                                                @RequestParam(required = false) String tag,
                                                @RequestParam(required = false) String q,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return taskQueryService.getTasks(userId, projectKey, status, assignee, priority, tag, q, page, size);
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@CurrentUser UUID userId, @PathVariable String projectKey,
                                    @Valid @RequestBody CreateTaskRequest request) {
        return taskCommandService.createTask(userId, projectKey, request);
    }

    @GetMapping("/tags")
    public List<TagResponse> getTags(@CurrentUser UUID userId, @PathVariable String projectKey) {
        return taskQueryService.getTags(userId, projectKey);
    }
}
