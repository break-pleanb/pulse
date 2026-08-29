package com.den.pulse.core.jpa;

import com.den.pulse.domain.notification.entity.NotificationType;

public class NotificationTypeJavaType extends NoCheckEnumJavaType<NotificationType> {

    public NotificationTypeJavaType() {
        super(NotificationType.class);
    }
}
