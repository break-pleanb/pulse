package com.den.pulse.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 브라우저 네이티브 WebSocket API는 핸드셰이크 요청에 커스텀 헤더(Authorization)를 실을 수 없어
 * 쿼리 파라미터(token)로 JWT를 전달받아 여기서 검증한다 (DEN-DESIGN.md 6.2절 "/ws JWT 핸드셰이크 인증").
 * 검증에 성공하면 userId를 세션 attributes에 담아 WsHandshakeHandler가 Principal로 승격한다.
 */
@Component
@RequiredArgsConstructor
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "userId";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        List<String> tokens = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().get("token");
        String token = tokens != null && !tokens.isEmpty() ? tokens.get(0) : null;
        if (token == null) {
            return false;
        }
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            if (!jwtTokenProvider.isAccessToken(claims)) {
                return false;
            }
            attributes.put(ATTR_USER_ID, jwtTokenProvider.getUserId(claims));
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }
}
