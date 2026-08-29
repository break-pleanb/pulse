package com.den.pulse.domain.channel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 채널을 지금 구독 중인(=보고 있는) 사용자를 Redis Set으로 추적한다
 * (CLAUDE.md 기술스택 "Spring Data Redis — WS 세션"의 용도가 바로 이것).
 * 메시지 발송 시 channel_message 알림 대상에서 "지금 보고 있는 멤버"를 제외하는 데 쓴다
 * (API-SPEC.md 6장 비고). ChannelPresenceEventListener가 STOMP 구독/해제/연결종료 이벤트에
 * 맞춰 갱신한다.
 */
@Service
@RequiredArgsConstructor
public class ChannelPresenceService {

    private static final String KEY_PREFIX = "channel:presence:";

    private final StringRedisTemplate redisTemplate;

    public void markPresent(UUID channelId, UUID userId) {
        redisTemplate.opsForSet().add(key(channelId), userId.toString());
    }

    public void markAbsent(UUID channelId, UUID userId) {
        redisTemplate.opsForSet().remove(key(channelId), userId.toString());
    }

    public boolean isPresent(UUID channelId, UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key(channelId), userId.toString()));
    }

    private String key(UUID channelId) {
        return KEY_PREFIX + channelId;
    }
}
