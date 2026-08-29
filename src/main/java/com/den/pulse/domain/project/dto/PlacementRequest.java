package com.den.pulse.domain.project.dto;

import java.util.UUID;

/**
 * folderId가 null이면 미분류로 이동 (API-SPEC.md 2장) — 값이 전송되지 않는 경우가 아니라
 * 항상 채워지는 필드이므로 "필드 미포함"과 "명시적 null"을 구분할 필요가 없다.
 */
public record PlacementRequest(UUID folderId) {
}
