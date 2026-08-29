package com.den.pulse.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MenuKey {
    @JsonProperty("tasks") TASKS,
    @JsonProperty("gantt") GANTT,
    @JsonProperty("messenger") MESSENGER
}
