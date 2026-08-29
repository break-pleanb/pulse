package com.den.pulse.domain.task.dto;

import com.den.pulse.domain.task.entity.TaskPriority;
import com.den.pulse.domain.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 최상위 업무 생성 (API-SPEC.md 4장, 2026-08-29 사용자 요청으로 추가).
 * title만 필수이고 나머지는 서버 기본값을 쓴다: status=todo, priority=medium, progress=0, isPrivate=false.
 */
public record CreateTaskRequest(
        @NotBlank(message = "제목을 입력해 주세요.")
        String title,
        TaskStatus status,
        TaskPriority priority,
        LocalDate startDate,
        LocalDate endDate,
        List<UUID> assigneeIds,
        List<UUID> tagIds,
        Boolean isPrivate
) {
}
