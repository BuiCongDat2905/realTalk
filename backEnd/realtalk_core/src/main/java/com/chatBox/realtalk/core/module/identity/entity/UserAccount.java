package com.chatBox.realtalk.core.module.identity.entity;

import com.chatBox.realtalk.base.baseEntity.BasePublicIdEntity;
import com.chatBox.realtalk.core.module.identity.enums.UserRole;
import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_name", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAccount extends BasePublicIdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    Long id;

    @Column(name = "username", nullable = false, length = 50)
    String username;

    @Column(name = "email", nullable = false, length = 255)
    String email;

    @Column(name = "password_hash", nullable = true, length = 255)
    String passwordHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", nullable = false, length = 20)
    UserRole systemRole = UserRole.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified_at")
    Instant emailVerifiedAt;

    @Column(name = "last_login_at")
    Instant lastLoginAt;

    @Column(name = "deleted_at")
    Instant deletedAt;

}
