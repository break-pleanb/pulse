package com.den.pulse.domain.user.entity;

import com.den.pulse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 테이블명을 "users"로 둔 이유: PostgreSQL에서 "user"는 예약어라 그대로 쓰면 충돌한다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String initials;

    @Column(name = "avatar_gradient", nullable = false)
    private String avatarGradient;

    private String title;

    public User(String email, String passwordHash, String name, String initials, String avatarGradient, String title) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.initials = initials;
        this.avatarGradient = avatarGradient;
        this.title = title;
    }
}
