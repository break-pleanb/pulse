package com.den.pulse.core.exception;

/**
 * 로그인 실패, 리프레시 토큰 만료/무효 등 인증 자체가 성립하지 않을 때 던진다.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
