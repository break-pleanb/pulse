package com.den.pulse.core.security;

import java.security.Principal;
import java.util.UUID;

/**
 * STOMP 핸드셰이크에서 인증한 userId를 세션의 Principal로 노출한다.
 * /user/queue/... 라우팅과 @MessageMapping의 Principal 파라미터가 이 값을 쓴다.
 */
public record WsPrincipal(UUID userId) implements Principal {

    @Override
    public String getName() {
        return userId.toString();
    }
}
