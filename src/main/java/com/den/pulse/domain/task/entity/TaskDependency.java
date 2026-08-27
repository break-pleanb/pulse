package com.den.pulse.domain.task.entity;

import com.den.pulse.core.entity.BaseEntity;
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
 * task는 dependsOn이 먼저 끝나야 시작할 수 있다 (간트 선행 업무). 순환 참조 검사는 서비스 계층에서 수행한다.
 */
@Entity
@Table(
        name = "task_dependency",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "depends_on_task_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskDependency extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOn;

    public TaskDependency(Task task, Task dependsOn) {
        this.task = task;
        this.dependsOn = dependsOn;
    }
}
