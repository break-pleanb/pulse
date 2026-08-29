package com.den.pulse.core.jpa;

import com.den.pulse.domain.task.entity.TaskStatus;

public class TaskStatusJavaType extends NoCheckEnumJavaType<TaskStatus> {

    public TaskStatusJavaType() {
        super(TaskStatus.class);
    }
}
