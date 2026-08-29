package com.den.pulse.domain.task.dto;

import java.util.UUID;

public record ProjectTaskStatsResponse(
        UUID projectId,
        long total,
        long todo,
        long progress,
        long review,
        long done
) {
}
