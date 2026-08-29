package com.den.pulse.domain.task.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TaskPriority {
    @JsonProperty("urgent") URGENT,
    @JsonProperty("high") HIGH,
    @JsonProperty("medium") MEDIUM,
    @JsonProperty("low") LOW
}
