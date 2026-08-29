package com.den.pulse.domain.task.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.task.dto.CreateSubtaskRequest;
import com.den.pulse.domain.task.dto.TaskActivityResponse;
import com.den.pulse.domain.task.dto.TaskResponse;
import com.den.pulse.domain.task.dto.UpdateAssigneesRequest;
import com.den.pulse.domain.task.dto.UpdateDependenciesRequest;
import com.den.pulse.domain.task.dto.UpdateTaskRequest;
import com.den.pulse.domain.task.dto.UpdateTaskStatusRequest;
import com.den.pulse.domain.task.service.TaskActivityService;
import com.den.pulse.domain.task.service.TaskCommandService;
import com.den.pulse.domain.task.service.TaskQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskQueryService taskQueryService;
    private final TaskCommandService taskCommandService;
    private final TaskActivityService taskActivityService;

    @GetMapping("/{taskId}")
    public TaskResponse getTask(@CurrentUser UUID userId, @PathVariable UUID taskId) {
        return taskQueryService.getTask(userId, taskId);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@CurrentUser UUID userId, @PathVariable UUID taskId) {
        taskCommandService.deleteTask(userId, taskId);
    }

    @GetMapping("/{taskId}/activities")
    public List<TaskActivityResponse> getActivities(@CurrentUser UUID userId, @PathVariable UUID taskId) {
        return taskActivityService.getActivities(userId, taskId);
    }

    @GetMapping("/{taskId}/subtask-count")
    public long getSubtaskCount(@CurrentUser UUID userId, @PathVariable UUID taskId) {
        return taskQueryService.getSubtaskCount(userId, taskId);
    }

    @PostMapping("/{parentId}/subtasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createSubtask(@CurrentUser UUID userId, @PathVariable UUID parentId,
                                       @Valid @RequestBody CreateSubtaskRequest request) {
        return taskCommandService.createSubtask(userId, parentId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(@CurrentUser UUID userId, @PathVariable UUID taskId,
                                      @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskCommandService.updateStatus(userId, taskId, request);
    }

    @PatchMapping("/{taskId}")
    public TaskResponse updateTask(@CurrentUser UUID userId, @PathVariable UUID taskId,
                                    @Valid @RequestBody UpdateTaskRequest request) {
        return taskCommandService.updateTask(userId, taskId, request);
    }

    @PatchMapping("/{taskId}/assignees")
    public TaskResponse updateAssignees(@CurrentUser UUID userId, @PathVariable UUID taskId,
                                         @Valid @RequestBody UpdateAssigneesRequest request) {
        return taskCommandService.updateAssignees(userId, taskId, request);
    }

    @PatchMapping("/{taskId}/dependencies")
    public TaskResponse updateDependencies(@CurrentUser UUID userId, @PathVariable UUID taskId,
                                            @Valid @RequestBody UpdateDependenciesRequest request) {
        return taskCommandService.updateDependencies(userId, taskId, request);
    }
}
