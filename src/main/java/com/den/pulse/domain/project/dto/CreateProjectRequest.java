package com.den.pulse.domain.project.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateProjectRequest(
        @NotBlank(message = "프로젝트 이름을 입력해 주세요.")
        String name,
        UUID folderId
) {
}
