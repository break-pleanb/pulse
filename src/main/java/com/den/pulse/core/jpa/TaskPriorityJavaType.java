package com.den.pulse.core.jpa;

import com.den.pulse.domain.task.entity.TaskPriority;

public class TaskPriorityJavaType extends NoCheckEnumJavaType<TaskPriority> {

    public TaskPriorityJavaType() {
        super(TaskPriority.class);
    }
}
