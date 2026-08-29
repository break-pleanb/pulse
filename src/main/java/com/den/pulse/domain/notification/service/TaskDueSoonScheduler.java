package com.den.pulse.domain.notification.service;

import com.den.pulse.domain.notification.entity.NotificationType;
import com.den.pulse.domain.notification.repository.NotificationRepository;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.repository.TaskAssigneeRepository;
import com.den.pulse.domain.task.repository.TaskRepository;
import com.den.pulse.domain.task.repository.TaskWatcherRepository;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * task_due_soon 알림(API-SPEC.md 7장) — endDate가 오늘·내일이고 status != done인 업무를 매일 스캔해
 * 담당자·참여자에게 알림을 만든다. 주기·중복 방지 정책은 스펙에 명시가 없어 2026-08-29 사용자와 합의:
 * 매일 09:00 1회 실행, 같은 업무·수신자 조합은 당일 이미 알림이 있으면 다시 만들지 않는다.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class TaskDueSoonScheduler {

    private static final String TITLE = "업무 마감이 임박했습니다";

    private final EntityManager entityManager;
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskWatcherRepository taskWatcherRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void notifyDueSoonTasks() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDateTime todayStart = today.atStartOfDay();

        for (Task task : taskRepository.findDueSoon(today, tomorrow)) {
            Set<UUID> recipientIds = new HashSet<>(taskAssigneeRepository.findUserIdsByTask_Id(task.getId()));
            recipientIds.addAll(taskWatcherRepository.findUserIdsByTask_Id(task.getId()));

            for (UUID uid : recipientIds) {
                boolean alreadyNotifiedToday = notificationRepository
                        .existsByUser_IdAndLinkTask_IdAndTypeAndCreatedAtGreaterThanEqual(
                                uid, task.getId(), NotificationType.TASK_DUE_SOON, todayStart);
                if (alreadyNotifiedToday) {
                    continue;
                }
                User user = entityManager.getReference(User.class, uid);
                notificationService.notify(user, NotificationType.TASK_DUE_SOON,
                        TITLE, task.getTitle(), task.getProject(), task, null);
            }
        }
    }
}
