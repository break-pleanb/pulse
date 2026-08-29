package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.TaskStatus;

import java.util.UUID;

/** /api/me/project-stats 집계용 — 프로젝트 × 상태별 업무 개수. */
public interface ProjectStatusCountView {

    UUID getProjectId();

    TaskStatus getStatus();

    long getCnt();
}
