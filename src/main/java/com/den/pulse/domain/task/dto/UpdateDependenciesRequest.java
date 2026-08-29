package com.den.pulse.domain.task.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateDependenciesRequest(
        @NotNull(message = "dependencyIds는 필수입니다.")
        List<UUID> dependencyIds
) {
}
