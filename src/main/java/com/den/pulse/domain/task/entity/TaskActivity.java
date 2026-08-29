package com.den.pulse.domain.task.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.domain.user.entity.User;
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

/**
 * 업무 필드 변경 이력 (API-SPEC.md 5장). oldValue/newValue는 필드 타입에 관계없이 문자열로 직렬화해
 * 저장한다 — 여러 타입(enum/날짜/숫자/불리언/UUID 목록)을 한 테이블에 담기 위함.
 */
@Entity
@Table(name = "task_activity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityField field;

    @Column(name = "old_value", nullable = false, columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", nullable = false, columnDefinition = "text")
    private String newValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    public TaskActivity(Task task, ActivityField field, String oldValue, String newValue, User changedBy) {
        this.task = task;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
    }
}
