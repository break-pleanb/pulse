package com.den.pulse.domain.channel.service;

import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.channel.dto.ChannelResponse;
import com.den.pulse.domain.channel.dto.CreateChannelRequest;
import com.den.pulse.domain.channel.dto.CreateDmRequest;
import com.den.pulse.domain.channel.dto.MessageResponse;
import com.den.pulse.domain.channel.dto.SendMessageRequest;
import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.channel.entity.ChannelMember;
import com.den.pulse.domain.channel.entity.ChannelType;
import com.den.pulse.domain.channel.entity.Message;
import com.den.pulse.domain.channel.repository.ChannelMemberRepository;
import com.den.pulse.domain.channel.repository.ChannelRepository;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.repository.ProjectMemberRepository;
import com.den.pulse.domain.member.service.ProjectAccessService;
import com.den.pulse.domain.notification.entity.NotificationType;
import com.den.pulse.domain.notification.service.NotificationService;
import com.den.pulse.domain.project.entity.Project;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 채널·DM 생성, 메시지 발송(STOMP 브로드캐스트 + channel_message 알림), 읽음 처리.
 * 채널·DM 생성 엔드포인트는 API-SPEC.md 6장에 원래 없었으나, 이대로면 메신저를 테스트할 방법이
 * 없어 2026-08-29 사용자와 합의해 신설했다(6단계 최상위 업무 생성 신설과 같은 패턴).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelCommandService {

    private static final String CHANNEL_NOT_FOUND_MESSAGE = "채널을 찾을 수 없습니다.";

    private final EntityManager entityManager;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final ChannelAccessService channelAccessService;
    private final NotificationService notificationService;
    private final ChannelPresenceService channelPresenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChannelResponse createChannel(UUID userId, String projectKey, CreateChannelRequest request) {
        ProjectMember requester = projectAccessService.requireMenuAccess(userId, projectKey, MenuKey.MESSENGER);
        Project project = requester.getProject();

        Set<UUID> memberIds = request.memberIds() != null
                ? new LinkedHashSet<>(request.memberIds()) : new LinkedHashSet<>();
        memberIds.add(userId);
        validateProjectMembers(project.getId(), memberIds);

        Channel channel = new Channel(project, request.name(), ChannelType.GROUP);
        entityManager.persist(channel);
        for (UUID uid : memberIds) {
            User user = entityManager.getReference(User.class, uid);
            entityManager.persist(new ChannelMember(channel, user));
        }

        return ChannelResponse.from(channel, List.copyOf(memberIds), 0);
    }

    /** 이미 같은 두 사람의 DM 채널이 있으면 그대로 반환한다(멱등) — 그 경우 컨트롤러가 200으로 응답한다. */
    @Transactional
    public ChannelResponse createDm(UUID userId, String projectKey, CreateDmRequest request) {
        ProjectMember requester = projectAccessService.requireMenuAccess(userId, projectKey, MenuKey.MESSENGER);
        Project project = requester.getProject();
        UUID targetUserId = request.targetUserId();
        if (targetUserId.equals(userId)) {
            throw new IllegalArgumentException("자기 자신과 DM을 시작할 수 없습니다.");
        }
        validateProjectMembers(project.getId(), Set.of(targetUserId));

        Channel existing = channelRepository
                .findDmChannel(project.getId(), ChannelType.DM, userId, targetUserId)
                .orElse(null);
        if (existing != null) {
            List<UUID> memberIds = channelMemberRepository.findUserIdsByChannel_Id(existing.getId());
            return ChannelResponse.from(existing, memberIds, 0);
        }

        User requesterUser = entityManager.getReference(User.class, userId);
        User targetUser = entityManager.getReference(User.class, targetUserId);
        List<String> names = new ArrayList<>(List.of(requesterUser.getName(), targetUser.getName()));
        Collections.sort(names);

        Channel channel = new Channel(project, String.join(", ", names), ChannelType.DM);
        entityManager.persist(channel);
        entityManager.persist(new ChannelMember(channel, requesterUser));
        entityManager.persist(new ChannelMember(channel, targetUser));

        return ChannelResponse.from(channel, List.of(userId, targetUserId), 0);
    }

    @Transactional
    public MessageResponse sendMessage(UUID userId, UUID channelId, SendMessageRequest request) {
        Channel channel = channelAccessService.requireVisibleChannel(userId, channelId);
        User author = entityManager.getReference(User.class, userId);

        Set<UUID> mentionUserIds = request.mentionUserIds() != null
                ? new LinkedHashSet<>(request.mentionUserIds()) : Set.of();
        validateChannelMembers(channelId, mentionUserIds);

        Message message = new Message(channel, author, request.body(), List.copyOf(mentionUserIds));
        entityManager.persist(message);

        MessageResponse response = MessageResponse.from(message);
        messagingTemplate.convertAndSend("/topic/channel/" + channelId, response);

        notifyAbsentMembers(channel, userId);
        return response;
    }

    @Transactional
    public void markRead(UUID userId, UUID channelId) {
        channelAccessService.requireVisibleChannel(userId, channelId);
        ChannelMember member = channelMemberRepository.findByChannel_IdAndUser_Id(channelId, userId)
                .orElseThrow(() -> new NotFoundException(CHANNEL_NOT_FOUND_MESSAGE));
        member.setLastReadAt(LocalDateTime.now());
    }

    /** 지금 그 채널을 구독 중(=보고 있는)인 멤버는 제외하고 channel_message 알림을 만든다 (API-SPEC.md 6장 비고). */
    private void notifyAbsentMembers(Channel channel, UUID authorId) {
        for (UUID memberId : channelMemberRepository.findUserIdsByChannel_Id(channel.getId())) {
            if (memberId.equals(authorId) || channelPresenceService.isPresent(channel.getId(), memberId)) {
                continue;
            }
            User recipient = entityManager.getReference(User.class, memberId);
            notificationService.notify(recipient, NotificationType.CHANNEL_MESSAGE,
                    "새 메시지가 도착했습니다", channel.getName(), channel.getProject(), null, channel);
        }
    }

    private void validateProjectMembers(UUID projectId, Set<UUID> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        long matched = projectMemberRepository.countByProject_IdAndUser_IdIn(projectId, userIds);
        if (matched != userIds.size()) {
            throw new IllegalArgumentException("프로젝트 멤버가 아닌 사용자는 채널에 추가할 수 없습니다.");
        }
    }

    private void validateChannelMembers(UUID channelId, Set<UUID> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        Set<UUID> channelMemberIds = new HashSet<>(channelMemberRepository.findUserIdsByChannel_Id(channelId));
        if (!channelMemberIds.containsAll(userIds)) {
            throw new IllegalArgumentException("채널 멤버가 아닌 사용자는 멘션할 수 없습니다.");
        }
    }
}
