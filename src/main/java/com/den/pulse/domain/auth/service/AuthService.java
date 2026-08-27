package com.den.pulse.domain.auth.service;

import com.den.pulse.core.exception.NotFoundException;
import com.den.pulse.core.exception.UnauthorizedException;
import com.den.pulse.core.security.JwtTokenProvider;
import com.den.pulse.domain.auth.dto.LoginRequest;
import com.den.pulse.domain.auth.dto.LoginResponse;
import com.den.pulse.domain.auth.dto.RefreshRequest;
import com.den.pulse.domain.auth.dto.RefreshResponse;
import com.den.pulse.domain.user.dto.UserResponse;
import com.den.pulse.domain.user.entity.User;
import com.den.pulse.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";
    private static final String INVALID_REFRESH_TOKEN_MESSAGE = "리프레시 토큰이 유효하지 않습니다.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        return new LoginResponse(accessToken, refreshToken, UserResponse.from(user));
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }

    public RefreshResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtTokenProvider.parseClaims(request.refreshToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN_MESSAGE);
        }
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        UUID userId = jwtTokenProvider.getUserId(claims);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN_MESSAGE));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        return new RefreshResponse(accessToken);
    }
}
