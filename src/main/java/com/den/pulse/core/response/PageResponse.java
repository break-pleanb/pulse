package com.den.pulse.core.response;

import java.util.List;

/**
 * 페이지네이션이 적용되는 목록 응답 전용 래퍼 (API-SPEC.md 0.3절).
 * 그 외 목록은 래퍼 없이 배열을 그대로 반환한다.
 */
public record PageResponse<T>(List<T> items, long total, int page, int size) {
}
