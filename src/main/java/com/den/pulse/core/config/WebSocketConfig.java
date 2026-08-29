package com.den.pulse.core.config;

import com.den.pulse.core.security.WsHandshakeHandler;
import com.den.pulse.core.security.WsHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * DEN-DESIGN.md 6.2절 STOMP 계약: /ws 연결(JWT 핸드셰이크), /topic 구독(채널 새 메시지), /user/queue
 * 구독(개인 알림 실시간), /app 발행(메시지 전송·타이핑). 실시간이 필요한 메신저·알림에만 국한한다
 * (그 외는 REST) — 원칙 그대로.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WsHandshakeInterceptor wsHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new WsHandshakeHandler())
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
