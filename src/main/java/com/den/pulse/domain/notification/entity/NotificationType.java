package com.den.pulse.domain.notification.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NotificationType {
    @JsonProperty("task_mention") TASK_MENTION,
    @JsonProperty("task_assigned") TASK_ASSIGNED,
    @JsonProperty("task_comment") TASK_COMMENT,
    @JsonProperty("task_due_soon") TASK_DUE_SOON,
    @JsonProperty("task_status_changed") TASK_STATUS_CHANGED,
    @JsonProperty("channel_message") CHANNEL_MESSAGE,
    @JsonProperty("project_invited") PROJECT_INVITED
}
