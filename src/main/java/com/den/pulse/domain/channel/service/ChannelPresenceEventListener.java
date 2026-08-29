package com.den.pulse.domain.channel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * STOMP 구독/구독해제/연결종료 이벤트를 감지해 ChannelPresenceService의 Redis Set을 갱신한다.
 * 구독 해제·연결종료 이벤트에는 destination이 실려오지 않으므로, 구독 시점에 (세션ID+구독ID) → (채널ID,
 * 사용자ID)를 로컬 맵에 기억해뒀다가 해제 시 꺼내 쓴다. 단일 인스턴스 개발 환경 기준 — 인스턴스를 여러 대로
 * 늘려도 한 세션의 구독/해제/연결종료 이벤트는 항상 같은 인스턴스에서 발생하므로 이 로컬 맵으로 충분하다.
 */
@Component
@RequiredArgsConstructor
public class ChannelPresenceEventListener {

    private static final Pattern CHANNEL_TOPIC_PATTERN = Pattern.compile("^/topic/channel/([0-9a-fA-F-]{36})$");

    private final ChannelPresenceService presenceService;
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        if (destination == null || user == null) {
            return;
        }
        Matcher matcher = CHANNEL_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }
        UUID channelId = UUID.fromString(matcher.group(1));
        UUID userId = UUID.fromString(user.getName());
        presenceService.markPresent(channelId, userId);
        subscriptions.put(subscriptionKey(accessor), new Subscription(channelId, userId));
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Subscription subscription = subscriptions.remove(subscriptionKey(accessor));
        if (subscription != null) {
            presenceService.markAbsent(subscription.channelId(), subscription.userId());
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        subscriptions.keySet().removeIf(key -> {
            if (!key.startsWith(sessionId + ":")) {
                return false;
            }
            Subscription subscription = subscriptions.get(key);
            if (subscription != null) {
                presenceService.markAbsent(subscription.channelId(), subscription.userId());
            }
            return true;
        });
    }

    private String subscriptionKey(StompHeaderAccessor accessor) {
        return accessor.getSessionId() + ":" + accessor.getSubscriptionId();
    }

    private record Subscription(UUID channelId, UUID userId) {
    }
}
