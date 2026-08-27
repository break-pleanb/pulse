package com.den.pulse.core.exception;

/**
 * 존재는 알지만 접근이 막힌 경우(메뉴 권한 등, 0.4절). 존재 자체를 숨겨야 하면 대신 NotFoundException을 쓴다.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
