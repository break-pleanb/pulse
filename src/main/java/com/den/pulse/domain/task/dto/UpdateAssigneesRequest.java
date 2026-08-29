package com.den.pulse.domain.task.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateAssigneesRequest(
        @NotNull(message = "assigneeIds는 필수입니다.")
        List<UUID> assigneeIds
) {
}
