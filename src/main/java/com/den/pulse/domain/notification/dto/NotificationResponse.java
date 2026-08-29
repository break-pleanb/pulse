package com.den.pulse.domain.notification.dto;

import com.den.pulse.domain.notification.entity.Notification;
import com.den.pulse.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType type,
        String title,
        String body,
        String projectKey,
        UUID linkTaskId,
        UUID linkChannelId,
        boolean isRead,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getProject() != null ? notification.getProject().getKey() : null,
                notification.getLinkTask() != null ? notification.getLinkTask().getId() : null,
                notification.getLinkChannel() != null ? notification.getLinkChannel().getId() : null,
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
