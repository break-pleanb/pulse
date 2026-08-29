package com.den.pulse.domain.notification.repository;

import com.den.pulse.domain.notification.entity.Notification;
import com.den.pulse.domain.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    long countByUser_IdAndReadFalse(UUID userId);

    List<Notification> findByUser_IdAndReadFalse(UUID userId);

    boolean existsByUser_IdAndLinkTask_IdAndTypeAndCreatedAtGreaterThanEqual(
            UUID userId, UUID linkTaskId, NotificationType type, LocalDateTime after);
}
