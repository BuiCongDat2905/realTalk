package com.chatBox.realtalk.core.module.identity.entity;

import com.chatBox.realtalk.base.baseEntity.BasePublicIdEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSessions extends BasePublicIdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @NotNull
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "token_hash", nullable = false, length = 32, columnDefinition = "BINARY(32)")
    private byte[] tokenHash;
    @Size(max = 100)
    @Column(name = "device_id", length = 100)
    private String deviceId;
    @Size(max = 255)
    @Column(name = "device_name")
    private String deviceName;
    @Size(max = 50)
    @Column(name = "device_type", length = 50)
    private String deviceType;
    @Size(max = 1000)
    @Column(name = "user_agent", length = 1000)
    private String userAgent;
    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "last_used_at")
    private Instant lastUsedAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;
    @Size(max = 255)
    @Column(name = "revoke_reason")
    private String revokeReason;

}
