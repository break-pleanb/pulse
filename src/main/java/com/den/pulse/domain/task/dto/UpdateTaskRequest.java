package com.den.pulse.domain.task.dto;

import com.den.pulse.domain.task.entity.TaskPriority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

/**
 * 부분 수정 — 요청에 들어온 필드만 세팅한다 (CLAUDE.md 7-3). 필드가 null이면 "요청에 없음"을 의미한다.
 * title/priority/startDate/endDate/progress/isPrivate 전부 엔티티에서 not-null 컬럼이라
 * "의도적으로 null로 지우기"를 표현할 필요가 없어 Optional 래핑 없이 null=미지정으로 충분하다.
 */
public record UpdateTaskRequest(
        String title,
        TaskPriority priority,
        LocalDate startDate,
        LocalDate endDate,
        @Min(value = 0, message = "progress는 0 이상이어야 합니다.")
        @Max(value = 100, message = "progress는 100 이하여야 합니다.")
        Integer progress,
        Boolean isPrivate
) {
}
