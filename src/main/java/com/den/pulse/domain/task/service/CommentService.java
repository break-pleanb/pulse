package com.den.pulse.domain.task.service;

import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.notification.entity.NotificationType;
import com.den.pulse.domain.notification.service.NotificationService;
import com.den.pulse.domain.task.dto.CommentResponse;
import com.den.pulse.domain.task.dto.CreateCommentRequest;
import com.den.pulse.domain.task.entity.Comment;
import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.repository.CommentRepository;
import com.den.pulse.domain.task.repository.TaskAssigneeRepository;
import com.den.pulse.domain.task.repository.TaskWatcherRepository;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 업무 댓글 조회·작성 (API-SPEC.md 5장). 작성 시 commentCount 증가와 멘션(task_mention)·
 * 댓글(task_comment) 알림 생성(7장 트리거 표)을 같은 트랜잭션에서 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final EntityManager entityManager;
    private final CommentRepository commentRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskWatcherRepository taskWatcherRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskAccessService taskAccessService;
    private final NotificationService notificationService;

    public List<CommentResponse> getComments(UUID userId, UUID taskId) {
        taskAccessService.requireVisibleTask(userId, taskId);
        return commentRepository.findByTask_IdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(UUID userId, UUID taskId, CreateCommentRequest request) {
        Task task = taskAccessService.requireVisibleTask(userId, taskId);
        User author = entityManager.getReference(User.class, userId);

        Set<UUID> mentionUserIds = request.mentionUserIds() != null
                ? new LinkedHashSet<>(request.mentionUserIds()) : Set.of();
        validateProjectMembers(task.getProject().getId(), mentionUserIds);

        Comment comment = new Comment(task, author, request.body(), List.copyOf(mentionUserIds));
        entityManager.persist(comment);

        task.setCommentCount(task.getCommentCount() + 1);

        notifyMentioned(task, mentionUserIds);
        notifyOtherParticipants(task, userId, mentionUserIds);

        return CommentResponse.from(comment);
    }

    private void notifyMentioned(Task task, Set<UUID> mentionUserIds) {
        for (UUID uid : mentionUserIds) {
            User user = entityManager.getReference(User.class, uid);
            notificationService.notify(user, NotificationType.TASK_MENTION,
                    "댓글에서 회원님을 멘션했습니다", task.getTitle(), task.getProject(), task, null);
        }
    }

    private void notifyOtherParticipants(Task task, UUID authorId, Set<UUID> mentionUserIds) {
        Set<UUID> recipientIds = new HashSet<>(taskAssigneeRepository.findUserIdsByTask_Id(task.getId()));
        recipientIds.addAll(taskWatcherRepository.findUserIdsByTask_Id(task.getId()));
        recipientIds.remove(authorId);
        recipientIds.removeAll(mentionUserIds);

        for (UUID uid : recipientIds) {
            User user = entityManager.getReference(User.class, uid);
            notificationService.notify(user, NotificationType.TASK_COMMENT,
                    "업무에 새 댓글이 달렸습니다", task.getTitle(), task.getProject(), task, null);
        }
    }

    private void validateProjectMembers(UUID projectId, Set<UUID> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        long matched = projectMemberRepository.countByProject_IdAndUser_IdIn(projectId, userIds);
        if (matched != userIds.size()) {
            throw new IllegalArgumentException("프로젝트 멤버가 아닌 사용자는 멘션할 수 없습니다.");
        }
    }
}
