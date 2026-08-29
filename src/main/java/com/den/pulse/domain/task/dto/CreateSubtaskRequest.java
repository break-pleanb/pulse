package com.den.pulse.domain.task.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubtaskRequest(
        @NotBlank(message = "제목을 입력해 주세요.")
        String title
) {
}
