package com.den.pulse.domain.project.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.project.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<UUID> myFavorites(@CurrentUser UUID userId) {
        return favoriteService.getMyFavoriteProjectIds(userId);
    }
}
