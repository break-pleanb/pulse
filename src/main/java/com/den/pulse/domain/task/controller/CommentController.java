package com.den.pulse.domain.task.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.task.dto.CommentResponse;
import com.den.pulse.domain.task.dto.CreateCommentRequest;
import com.den.pulse.domain.task.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentResponse> getComments(@CurrentUser UUID userId, @PathVariable UUID taskId) {
        return commentService.getComments(userId, taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@CurrentUser UUID userId, @PathVariable UUID taskId,
                                       @Valid @RequestBody CreateCommentRequest request) {
        return commentService.addComment(userId, taskId, request);
    }
}
