package com.den.pulse.domain.channel.controller;

import com.den.pulse.domain.channel.dto.SendMessageRequest;
import com.den.pulse.domain.channel.service.ChannelCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * REST POST /channels/{channelId}/messages와 동일한 동작을 STOMP로도 제공한다
 * (API-SPEC.md 8장 — "메시지 전송 (REST POST 대신 또는 함께 사용 가능)").
 * 타이핑 표시는 별도 토픽(/topic/channel/{channelId}/typing)에 발행한다 — 메시지 토픽
 * (/topic/channel/{channelId})은 Message 타입 계약(API-SPEC.md 9장)을 유지해야 해서 섞지 않았다.
 * 두 항목 모두 DEN-DESIGN.md 6.2절에 발행 목적지만 정의돼 있고 타이핑의 구독 목적지는 없어서
 * 이번 단계에서 구현하며 정한 것이다.
 */
@Controller
@RequiredArgsConstructor
public class ChannelWebSocketController {

    private final ChannelCommandService channelCommandService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/channel/{channelId}/send")
    public void send(@DestinationVariable UUID channelId, Principal principal, @Payload SendMessageRequest request) {
        UUID userId = UUID.fromString(principal.getName());
        channelCommandService.sendMessage(userId, channelId, request);
    }

    @MessageMapping("/channel/{channelId}/typing")
    public void typing(@DestinationVariable UUID channelId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        Map<String, String> payload = Map.of("userId", userId.toString(), "at", Instant.now().toString());
        messagingTemplate.convertAndSend("/topic/channel/" + channelId + "/typing", payload);
    }
}
