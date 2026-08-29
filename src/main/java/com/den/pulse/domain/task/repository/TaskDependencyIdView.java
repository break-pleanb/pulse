package com.den.pulse.domain.task.repository;

import java.util.UUID;

public interface TaskDependencyIdView {

    UUID getTaskId();

    UUID getDependsOnId();
}
