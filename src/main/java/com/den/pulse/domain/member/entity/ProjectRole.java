package com.den.pulse.domain.member.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * project가 null이면 전역 기본 역할(관리자/편집자/뷰어), 값이 있으면 해당 프로젝트 전용 커스텀 역할
 * (DEN-DESIGN.md 5.5절 절충안 — 설정하지 않으면 전역 기본값을 따른다).
 */
@Entity
@Table(name = "project_role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    public ProjectRole(Project project, String name, boolean admin) {
        this.project = project;
        this.name = name;
        this.admin = admin;
    }
}
