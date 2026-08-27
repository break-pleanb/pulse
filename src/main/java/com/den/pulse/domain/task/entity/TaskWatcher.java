package com.den.pulse.domain.task.entity;

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

/**
 * 참여자(watcher)는 비공개 업무 접근권 + 알림 수신 대상이라는 이중 역할을 가진다 (DEN-DESIGN.md 4.2절).
 */
@Entity
@Table(
        name = "task_watcher",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskWatcher extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public TaskWatcher(Task task, User user) {
        this.task = task;
        this.user = user;
    }
}
