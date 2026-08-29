package com.den.pulse.domain.channel.service;

import com.den.pulse.domain.channel.dto.ChannelResponse;
import com.den.pulse.domain.channel.dto.MessageResponse;
import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.channel.entity.ChannelMember;
import com.den.pulse.domain.channel.repository.ChannelMemberIdsView;
import com.den.pulse.domain.channel.repository.ChannelMemberRepository;
import com.den.pulse.domain.channel.repository.ChannelRepository;
import com.den.pulse.domain.channel.repository.MessageRepository;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectMember;
import com.den.pulse.domain.member.service.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 채널 목록·안읽음 수·메시지 조회 (API-SPEC.md 6장). unreadCount는 요청자의 채널별 lastReadAt 이후
 * 메시지 수로 계산한다 — lastReadAt이 채널마다 달라 배치 조인으로 한 번에 묶기 어려우므로, 채널별로
 * 카운트 쿼리를 따로 날린다(프로젝트당 채널 수가 많지 않다고 보고 단순 반복으로 처리).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelQueryService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final MessageRepository messageRepository;
    private final ProjectAccessService projectAccessService;
    private final ChannelAccessService channelAccessService;

    public List<ChannelResponse> getChannels(UUID userId, String projectKey) {
        ProjectMember member = projectAccessService.requireMenuAccess(userId, projectKey, MenuKey.MESSENGER);
        List<Channel> channels = channelRepository.findByProject_IdAndMemberUserId(member.getProject().getId(), userId);
        if (channels.isEmpty()) {
            return List.of();
        }

        List<UUID> channelIds = channels.stream().map(Channel::getId).toList();
        Map<UUID, List<UUID>> memberIdsByChannel = channelMemberRepository.findByChannelIdIn(channelIds).stream()
                .collect(Collectors.groupingBy(
                        ChannelMemberIdsView::getChannelId,
                        Collectors.mapping(ChannelMemberIdsView::getUserId, Collectors.toList())
                ));

        return channels.stream()
                .map(channel -> ChannelResponse.from(
                        channel,
                        memberIdsByChannel.getOrDefault(channel.getId(), List.of()),
                        unreadCount(channel.getId(), userId)
                ))
                .toList();
    }

    public long getUnreadCount(UUID userId, String projectKey) {
        ProjectMember member = projectAccessService.requireMenuAccess(userId, projectKey, MenuKey.MESSENGER);
        List<Channel> channels = channelRepository.findByProject_IdAndMemberUserId(member.getProject().getId(), userId);
        return channels.stream().mapToLong(channel -> unreadCount(channel.getId(), userId)).sum();
    }

    public List<MessageResponse> getMessages(UUID userId, UUID channelId) {
        channelAccessService.requireVisibleChannel(userId, channelId);
        return messageRepository.findByChannel_IdOrderByCreatedAtAsc(channelId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    private int unreadCount(UUID channelId, UUID userId) {
        LocalDateTime lastReadAt = channelMemberRepository.findByChannel_IdAndUser_Id(channelId, userId)
                .map(ChannelMember::getLastReadAt)
                .orElse(null);
        long count = lastReadAt != null
                ? messageRepository.countByChannel_IdAndCreatedAtAfter(channelId, lastReadAt)
                : messageRepository.countByChannel_Id(channelId);
        return (int) count;
    }
}
