package com.den.pulse.domain.member.entity;

import com.den.pulse.core.entity.BaseEntity;
import com.den.pulse.core.jpa.MenuKeyJavaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JavaType;

@Entity
@Table(
        name = "role_menu_perm",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "menu_key"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleMenuPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private ProjectRole role;

    @Enumerated(EnumType.STRING)
    @JavaType(MenuKeyJavaType.class)
    @Column(name = "menu_key", nullable = false)
    private MenuKey menuKey;

    @Setter
    @Column(nullable = false)
    private boolean enabled;

    public RoleMenuPermission(ProjectRole role, MenuKey menuKey, boolean enabled) {
        this.role = role;
        this.menuKey = menuKey;
        this.enabled = enabled;
    }
}
