package com.den.pulse.domain.notification.service;

import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.notification.entity.Notification;
import com.den.pulse.domain.notification.entity.NotificationType;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 알림 생성 (API-SPEC.md 7장 트리거 테이블). 조회·읽음 처리 API는 7단계(댓글·알림)에서 구현하고,
 * 그 전에도 다른 도메인(6단계 업무 등)에서 트리거되는 알림은 이 서비스로 미리 적재해 둔다.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EntityManager entityManager;

    public void notify(User user, NotificationType type, String title, String body,
                        Project project, Task linkTask, Channel linkChannel) {
        entityManager.persist(new Notification(user, type, title, body, project, linkTask, linkChannel));
    }
}
