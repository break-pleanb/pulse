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

@Entity
@Table(
        name = "task_tag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "tag_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public TaskTag(Task task, Tag tag) {
        this.task = task;
        this.tag = tag;
    }
}
