package com.den.pulse.domain.task.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TaskStatus {
    @JsonProperty("todo") TODO,
    @JsonProperty("progress") PROGRESS,
    @JsonProperty("review") REVIEW,
    @JsonProperty("done") DONE
}
