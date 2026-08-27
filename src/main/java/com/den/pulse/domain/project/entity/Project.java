package com.den.pulse.domain.project.entity;

import com.den.pulse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * folderId(개인 배치)·memberIds는 컬럼이 아니라 ProjectPlacement/ProjectMember에서 파생된다 (DEN-DESIGN.md 4.2절).
 */
@Entity
@Table(name = "project")
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

    public Project(String key, String name, String description, String color) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.color = color;
    }
}
