package com.den.pulse.domain.task.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * assigneeIds/watcherIds/dependencyIds/tagIds는 컬럼이 아니라 각 조인 테이블(TaskAssignee 등)에서 파생된다.
 * deletedAt은 소프트 삭제 마커 — @SQLRestriction으로 이 엔티티가 관여하는 모든 조회(HQL·Criteria·연관관계
 * 로딩 포함)에 자동으로 "deleted_at is null" 조건이 붙는다. 삭제된 업무는 findById 등에서 그냥 안 보인다.
 */
@Entity
@Table(name = "task")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Task parent;

    @Setter
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Setter
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Setter
    @Column(nullable = false)
    private int progress;

    @Setter
    @Column(name = "is_private", nullable = false)
    private boolean isPrivate;

    @Setter
    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Task(Project project, String code, String title, TaskStatus status, TaskPriority priority,
                Task parent, LocalDate startDate, LocalDate endDate, int progress, boolean isPrivate) {
        this.project = project;
        this.code = code;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.parent = parent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.isPrivate = isPrivate;
        this.commentCount = 0;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
