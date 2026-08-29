package com.den.pulse.domain.notification.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.notification.dto.NotificationResponse;
import com.den.pulse.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(@CurrentUser UUID userId) {
        return notificationService.getNotifications(userId);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@CurrentUser UUID userId) {
        return notificationService.getUnreadCount(userId);
    }

    @PostMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@CurrentUser UUID userId, @PathVariable UUID notificationId) {
        notificationService.markRead(userId, notificationId);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@CurrentUser UUID userId) {
        notificationService.markAllRead(userId);
    }
}
