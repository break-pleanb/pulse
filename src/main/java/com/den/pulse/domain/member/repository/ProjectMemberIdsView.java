package com.den.pulse.domain.member.repository;

import java.util.UUID;

/**
 * 프로젝트 멤버의 User 엔티티를 로딩하지 않고 (projectId, userId) 쌍만 배치 조회하기 위한 프로젝션.
 */
public interface ProjectMemberIdsView {

    UUID getProjectId();

    UUID getUserId();
}
