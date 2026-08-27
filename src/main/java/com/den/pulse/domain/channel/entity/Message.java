package com.den.pulse.domain.channel.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mention_user_ids", nullable = false)
    private List<UUID> mentionUserIds;

    public Message(Channel channel, User author, String body, List<UUID> mentionUserIds) {
        this.channel = channel;
        this.author = author;
        this.body = body;
        this.mentionUserIds = mentionUserIds;
    }
}
