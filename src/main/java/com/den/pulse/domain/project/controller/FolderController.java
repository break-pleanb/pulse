package com.den.pulse.domain.project.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.project.dto.CreateFolderRequest;
import com.den.pulse.domain.project.dto.FolderResponse;
import com.den.pulse.domain.project.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    public List<FolderResponse> myFolders(@CurrentUser UUID userId) {
        return folderService.getMyFolders(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponse createFolder(@CurrentUser UUID userId, @Valid @RequestBody CreateFolderRequest request) {
        return folderService.createFolder(userId, request);
    }
}
