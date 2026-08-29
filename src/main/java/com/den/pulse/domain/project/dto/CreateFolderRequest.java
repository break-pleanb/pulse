package com.den.pulse.domain.project.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(
        @NotBlank(message = "폴더 이름을 입력해 주세요.")
        String name
) {
}
