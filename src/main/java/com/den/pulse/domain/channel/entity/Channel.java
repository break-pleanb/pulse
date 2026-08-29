package com.den.pulse.domain.channel.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.core.jpa.ChannelTypeJavaType;
import com.den.pulse.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JavaType;

@Entity
@Table(name = "channel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JavaType(ChannelTypeJavaType.class)
    @Column(nullable = false)
    private ChannelType type;

    public Channel(Project project, String name, ChannelType type) {
        this.project = project;
        this.name = name;
        this.type = type;
    }
}
