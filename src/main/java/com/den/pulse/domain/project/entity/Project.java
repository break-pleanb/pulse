package com.den.pulse.domain.project.entity;

import com.den.pulse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * folderId(개인 배치)·memberIds는 컬럼이 아니라 ProjectPlacement/ProjectMember에서 파생된다 (DEN-DESIGN.md 4.2절).
 * deletedAt은 소프트 삭제 마커 — @SQLRestriction으로 이 엔티티가 관여하는 모든 조회에 자동으로
 * "deleted_at is null" 조건이 붙는다 (Task와 동일한 패턴, Task.java 주석 참고).
 */
@Entity
@Table(name = "project")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String color;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Project(String key, String name, String description, String color) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.color = color;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
