package com.den.pulse.domain.channel.repository;

import java.util.UUID;

/**
 * ChannelMember의 User 엔티티를 로딩하지 않고 (channelId, userId) 쌍만 배치 조회하기 위한 프로젝션.
 */
public interface ChannelMemberIdsView {

    UUID getChannelId();

    UUID getUserId();
}
