package com.den.pulse.domain.member.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.domain.project.entity.Project;
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

import java.time.LocalDate;

@Entity
@Table(
        name = "project_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private ProjectRole role;

    @Column(name = "invited_at", nullable = false)
    private LocalDate invitedAt;

    public ProjectMember(Project project, User user, ProjectRole role, LocalDate invitedAt) {
        this.project = project;
        this.user = user;
        this.role = role;
        this.invitedAt = invitedAt;
    }
}
