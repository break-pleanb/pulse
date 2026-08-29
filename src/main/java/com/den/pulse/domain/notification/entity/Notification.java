package com.den.pulse.domain.notification.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.core.jpa.NotificationTypeJavaType;
import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JavaType;

/**
 * project/linkTask/linkChannel은 알림 유형에 따라 일부만 채워진다 (API-SPEC.md 9장 AppNotification 참고).
 */
@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JavaType(NotificationTypeJavaType.class)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_task_id")
    private Task linkTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_channel_id")
    private Channel linkChannel;

    @Setter
    @Column(name = "is_read", nullable = false)
    private boolean read;

    public Notification(User user, NotificationType type, String title, String body,
                         Project project, Task linkTask, Channel linkChannel) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.project = project;
        this.linkTask = linkTask;
        this.linkChannel = linkChannel;
        this.read = false;
    }
}
