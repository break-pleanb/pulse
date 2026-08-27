package com.den.pulse.core.exception;

/**
 * 리소스가 없거나(진짜 없음), 요청자에게 접근 권한이 없어 존재를 숨겨야 할 때(0.4절) 던진다.
 * 두 경우를 프론트에서 구분할 필요가 없으므로 동일한 예외·동일한 404로 처리한다.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
