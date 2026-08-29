package com.den.pulse.domain.notification.service;

import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.notification.dto.NotificationResponse;
import com.den.pulse.domain.notification.entity.Notification;
import com.den.pulse.domain.notification.entity.NotificationType;
import com.den.pulse.domain.notification.repository.NotificationRepository;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 알림 생성(API-SPEC.md 7장 트리거 테이블)과 조회·읽음 처리 API(7단계). 다른 도메인(업무·댓글·멤버 등)에서
 * 트리거되는 알림도 이 서비스의 notify()로 생성한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final String NOTIFICATION_NOT_FOUND_MESSAGE = "알림을 찾을 수 없습니다.";

    private final EntityManager entityManager;
    private final NotificationRepository notificationRepository;

    public void notify(User user, NotificationType type, String title, String body,
                        Project project, Task linkTask, Channel linkChannel) {
        entityManager.persist(new Notification(user, type, title, body, project, linkTask, linkChannel));
    }

    public List<NotificationResponse> getNotifications(UUID userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND_MESSAGE));
        notification.setRead(true);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        for (Notification notification : notificationRepository.findByUser_IdAndReadFalse(userId)) {
            notification.setRead(true);
        }
    }
}
