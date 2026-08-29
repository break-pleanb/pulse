package com.den.pulse.domain.task.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ActivityField {
    @JsonProperty("status") STATUS,
    @JsonProperty("priority") PRIORITY,
    @JsonProperty("title") TITLE,
    @JsonProperty("startDate") START_DATE,
    @JsonProperty("endDate") END_DATE,
    @JsonProperty("progress") PROGRESS,
    @JsonProperty("assignees") ASSIGNEES,
    @JsonProperty("isPrivate") IS_PRIVATE
}
