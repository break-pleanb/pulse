package com.den.pulse.core.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/** WsHandshakeInterceptor가 attributes에 담아둔 userId를 세션 Principal로 승격한다. */
public class WsHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) {
        UUID userId = (UUID) attributes.get(WsHandshakeInterceptor.ATTR_USER_ID);
        return new WsPrincipal(userId);
    }
}
