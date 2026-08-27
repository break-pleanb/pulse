package com.den.pulse.domain.project.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 화면에서 프로젝트의 폴더 배치 (개인용). folder가 null이면 미분류.
 */
@Entity
@Table(
        name = "project_placement",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectPlacement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    public ProjectPlacement(User user, Project project, Folder folder) {
        this.user = user;
        this.project = project;
        this.folder = folder;
    }
}
