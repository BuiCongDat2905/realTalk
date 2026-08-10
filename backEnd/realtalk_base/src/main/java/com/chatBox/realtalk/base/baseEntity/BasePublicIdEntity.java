package com.chatBox.realtalk.base.baseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BasePublicIdEntity extends BaseEntity {
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
            name = "public_id",
            nullable = false,
            unique = true,
            updatable = false,
            columnDefinition = "BINARY(16)"
    )
    private UUID publicId;

    protected void generatePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }
}
