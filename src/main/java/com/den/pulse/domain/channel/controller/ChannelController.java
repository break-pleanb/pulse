package com.den.pulse.domain.channel.controller;

import com.den.pulse.core.security.CurrentUser;
import com.den.pulse.domain.channel.dto.MessageResponse;
import com.den.pulse.domain.channel.dto.SendMessageRequest;
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
@RequestMapping("/api/channels/{channelId}")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelQueryService channelQueryService;
    private final ChannelCommandService channelCommandService;

    @GetMapping("/messages")
    public List<MessageResponse> getMessages(@CurrentUser UUID userId, @PathVariable UUID channelId) {
        return channelQueryService.getMessages(userId, channelId);
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(@CurrentUser UUID userId, @PathVariable UUID channelId,
                                        @Valid @RequestBody SendMessageRequest request) {
        return channelCommandService.sendMessage(userId, channelId, request);
    }

    @PostMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@CurrentUser UUID userId, @PathVariable UUID channelId) {
        channelCommandService.markRead(userId, channelId);
    }
}
