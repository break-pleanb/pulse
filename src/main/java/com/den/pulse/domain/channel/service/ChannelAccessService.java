package com.den.pulse.domain.channel.service;

import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.channel.repository.ChannelMemberRepository;
import com.den.pulse.domain.channel.repository.ChannelRepository;
import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.service.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * channelId로 진입하는 메신저 하위 리소스 전반에서 공통으로 쓰는 접근 권한 체크
 * (CLAUDE.md "권한 체크를 컨트롤러마다 중복 구현 (공통화할 것)", TaskAccessService와 동일한 패턴).
 * 1) 프로젝트 멤버가 아니면 404, 2) 역할의 messenger 메뉴 권한이 꺼져 있으면 403,
 * 3) 요청자가 그 채널의 멤버가 아니면 404 — 그룹·DM 모두 채널 멤버십으로 통일해서 판단한다
 * (DM은 참여자 두 명만 멤버로 등록되므로 이 체크만으로 자연스럽게 비참여자를 막는다).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelAccessService {

    private static final String CHANNEL_NOT_FOUND_MESSAGE = "채널을 찾을 수 없습니다.";

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final ProjectAccessService projectAccessService;

    public Channel requireVisibleChannel(UUID userId, UUID channelId) {
        Channel channel = findChannel(channelId);
        projectAccessService.requireMenuAccess(userId, channel.getProject().getId(), MenuKey.MESSENGER);
        if (!channelMemberRepository.existsByChannel_IdAndUser_Id(channelId, userId)) {
            throw new NotFoundException(CHANNEL_NOT_FOUND_MESSAGE);
        }
        return channel;
    }

    private Channel findChannel(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException(CHANNEL_NOT_FOUND_MESSAGE));
    }
}
