package com.den.pulse.domain.channel.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * lastReadAt 이후 메시지 수로 채널 unreadCount를 계산한다 (API-SPEC.md 6장).
 */
@Entity
@Table(
        name = "channel_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    public ChannelMember(Channel channel, User user) {
        this.channel = channel;
        this.user = user;
        this.lastReadAt = null;
    }
}
