package com.den.pulse.domain.channel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ChannelType {
    @JsonProperty("group") GROUP,
    @JsonProperty("dm") DM
}
