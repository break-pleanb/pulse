package com.den.pulse.domain.user.service;

import com.den.pulse.domain.user.dto.UserResponse;
import com.den.pulse.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> searchUsers(String q) {
        return userRepository.searchByNameOrEmail(q).stream()
                .map(UserResponse::from)
                .toList();
    }
}
