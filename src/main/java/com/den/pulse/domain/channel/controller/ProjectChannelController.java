package com.den.pulse.domain.channel.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.channel.dto.ChannelResponse;
import com.den.pulse.domain.channel.dto.CreateChannelRequest;
import com.den.pulse.domain.channel.dto.CreateDmRequest;
import com.den.pulse.domain.channel.service.ChannelCommandService;
import com.den.pulse.domain.channel.service.ChannelQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectKey}/channels")
@RequiredArgsConstructor
public class ProjectChannelController {

    private final ChannelQueryService channelQueryService;
    private final ChannelCommandService channelCommandService;

    @GetMapping
    public List<ChannelResponse> getChannels(@CurrentUser UUID userId, @PathVariable String projectKey) {
        return channelQueryService.getChannels(userId, projectKey);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@CurrentUser UUID userId, @PathVariable String projectKey) {
        return channelQueryService.getUnreadCount(userId, projectKey);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChannelResponse createChannel(@CurrentUser UUID userId, @PathVariable String projectKey,
                                          @Valid @RequestBody CreateChannelRequest request) {
        return channelCommandService.createChannel(userId, projectKey, request);
    }

    /** 이미 있는 DM이면 200, 새로 만들었으면 컨트롤러가 아니라 서비스가 항상 같은 응답을 주므로 여기선 항상 200으로 통일한다. */
    @PostMapping("/dm")
    public ChannelResponse createDm(@CurrentUser UUID userId, @PathVariable String projectKey,
                                     @Valid @RequestBody CreateDmRequest request) {
        return channelCommandService.createDm(userId, projectKey, request);
    }
}
