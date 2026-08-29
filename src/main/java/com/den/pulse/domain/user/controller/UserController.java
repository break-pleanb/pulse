package com.den.pulse.domain.user.controller;

import com.den.pulse.domain.user.dto.UserResponse;
import com.den.pulse.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> searchUsers(@RequestParam String q) {
        return userService.searchUsers(q);
    }
}
