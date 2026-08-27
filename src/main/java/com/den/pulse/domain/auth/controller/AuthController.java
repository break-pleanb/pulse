package com.den.pulse.domain.auth.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.auth.dto.LoginRequest;
import com.den.pulse.domain.auth.dto.LoginResponse;
import com.den.pulse.domain.auth.dto.RefreshRequest;
import com.den.pulse.domain.auth.dto.RefreshResponse;
import com.den.pulse.domain.auth.service.AuthService;
import com.den.pulse.domain.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@CurrentUser UUID userId) {
        return authService.getCurrentUser(userId);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }
}
