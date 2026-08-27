package com.den.pulse.domain.task.entity;

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

/**
 * mentionUserIds는 DEN-DESIGN.md 4.1절 설계대로 별도 조인 테이블이 아닌 JSON 컬럼으로 저장한다.
 */
@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mention_user_ids", nullable = false)
    private List<UUID> mentionUserIds;

    public Comment(Task task, User author, String body, List<UUID> mentionUserIds) {
        this.task = task;
        this.author = author;
        this.body = body;
        this.mentionUserIds = mentionUserIds;
    }
}
