package com.den.pulse.domain.task.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record CreateCommentRequest(
        @NotBlank(message = "내용을 입력해 주세요.")
        String body,
        List<UUID> mentionUserIds
) {
}
