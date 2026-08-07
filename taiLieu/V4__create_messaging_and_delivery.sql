-- ============================================================================
-- RealTalk - V3: Messaging, attachments, notification, audit and outbox
-- ============================================================================

CREATE TABLE messages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    conversation_id BIGINT UNSIGNED NOT NULL,
    sender_user_id BIGINT UNSIGNED NOT NULL,
    sequence BIGINT UNSIGNED NOT NULL,

    -- React client should send crypto.randomUUID(); backend stores its 16 bytes.
    client_message_id BINARY(16) NOT NULL,

    type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    content TEXT NULL,
    reply_to_message_id BIGINT UNSIGNED NULL,
    recalled_at DATETIME(6) NULL,
    recalled_by_user_id BIGINT UNSIGNED NULL,
    edited_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,

    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT uk_messages_public_id UNIQUE (public_id),
    CONSTRAINT uk_messages_conversation_sequence
        UNIQUE (conversation_id, sequence),
    CONSTRAINT uk_messages_sender_client_id
        UNIQUE (sender_user_id, client_message_id),
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_messages_reply_to
        FOREIGN KEY (reply_to_message_id) REFERENCES messages(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_messages_recalled_by
        FOREIGN KEY (recalled_by_user_id) REFERENCES users(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_messages_type CHECK (
        type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM')
    ),
    CONSTRAINT chk_messages_text_content CHECK (
        type <> 'TEXT'
        OR (content IS NOT NULL AND CHAR_LENGTH(content) > 0)
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- The unique (conversation_id, sequence) index already supports cursor paging.
CREATE INDEX idx_messages_sender_created
    ON messages(sender_user_id, created_at DESC, id DESC);

CREATE INDEX idx_messages_reply_to
    ON messages(reply_to_message_id);

-- Plaintext search for MVP. Can later be replaced by Elasticsearch without
-- changing the messages table contract.
CREATE FULLTEXT INDEX ft_messages_content
    ON messages(content);

CREATE TABLE attachments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    owner_user_id BIGINT UNSIGNED NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(150) NOT NULL,
    file_size BIGINT UNSIGNED NOT NULL,
    checksum_sha256 BINARY(32) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    width INT UNSIGNED NULL,
    height INT UNSIGNED NULL,
    completed_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,

    CONSTRAINT pk_attachments PRIMARY KEY (id),
    CONSTRAINT uk_attachments_public_id UNIQUE (public_id),
    CONSTRAINT uk_attachments_storage_key UNIQUE (storage_key),
    CONSTRAINT fk_attachments_owner
        FOREIGN KEY (owner_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_attachments_status CHECK (
        status IN (
            'PENDING',
            'UPLOADED',
            'SCANNING',
            'AVAILABLE',
            'REJECTED',
            'DELETED'
        )
    ),
    CONSTRAINT chk_attachments_file_size CHECK (
        file_size > 0
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attachments_owner_created
    ON attachments(owner_user_id, created_at DESC, id DESC);

CREATE INDEX idx_attachments_status_cleanup
    ON attachments(status, created_at, id);

CREATE TABLE message_attachments (
    message_id BIGINT UNSIGNED NOT NULL,
    attachment_id BIGINT UNSIGNED NOT NULL,
    display_order SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_message_attachments
        PRIMARY KEY (message_id, attachment_id),
    CONSTRAINT uk_message_attachments_order
        UNIQUE (message_id, display_order),
    CONSTRAINT fk_message_attachments_message
        FOREIGN KEY (message_id) REFERENCES messages(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_attachment
        FOREIGN KEY (attachment_id) REFERENCES attachments(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_message_attachments_attachment
    ON message_attachments(attachment_id, message_id);

-- Sparse table: create a row only when a user stars or deletes-for-me.
CREATE TABLE message_user_states (
    message_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    deleted_at DATETIME(6) NULL,
    starred_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_message_user_states
        PRIMARY KEY (message_id, user_id),
    CONSTRAINT fk_message_user_states_message
        FOREIGN KEY (message_id) REFERENCES messages(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_message_user_states_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_message_user_states_has_state CHECK (
        deleted_at IS NOT NULL OR starred_at IS NOT NULL
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_message_user_states_user_deleted
    ON message_user_states(user_id, deleted_at, message_id);

CREATE INDEX idx_message_user_states_user_starred
    ON message_user_states(user_id, starred_at, message_id);

CREATE TABLE push_subscriptions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    endpoint TEXT NOT NULL,
    endpoint_hash BINARY(32) NOT NULL,
    p256dh_key VARCHAR(255) NOT NULL,
    auth_key VARCHAR(255) NOT NULL,
    user_agent VARCHAR(1000) NULL,
    device_name VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME(6) NULL,
    last_success_at DATETIME(6) NULL,
    last_failure_at DATETIME(6) NULL,
    failure_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_push_subscriptions PRIMARY KEY (id),
    CONSTRAINT uk_push_subscriptions_public_id UNIQUE (public_id),
    CONSTRAINT uk_push_subscriptions_endpoint_hash UNIQUE (endpoint_hash),
    CONSTRAINT fk_push_subscriptions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_push_subscriptions_status CHECK (
        status IN ('ACTIVE', 'INVALID', 'DISABLED')
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_push_subscriptions_user_dispatch
    ON push_subscriptions(user_id, status, expires_at, id);

CREATE TABLE notifications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NULL,
    body VARCHAR(1000) NULL,
    actor_user_id BIGINT UNSIGNED NULL,
    conversation_id BIGINT UNSIGNED NULL,
    message_id BIGINT UNSIGNED NULL,
    payload JSON NULL,
    read_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT uk_notifications_public_id UNIQUE (public_id),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor
        FOREIGN KEY (actor_user_id) REFERENCES users(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_notifications_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_notifications_message
        FOREIGN KEY (message_id) REFERENCES messages(id)
        ON DELETE SET NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, read_at, created_at DESC, id DESC);

CREATE INDEX idx_notifications_conversation
    ON notifications(conversation_id, created_at DESC, id DESC);

-- Audit is append-only. actor_user_id intentionally has no FK so a future
-- anonymization or hard-delete operation cannot erase the historical record.
CREATE TABLE audit_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BINARY(16) NOT NULL,
    actor_user_id BIGINT UNSIGNED NULL,
    actor_public_id BINARY(16) NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NULL,
    target_public_id BINARY(16) NULL,
    payload JSON NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(1000) NULL,
    correlation_id VARCHAR(64) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),
    CONSTRAINT uk_audit_logs_event_id UNIQUE (event_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_actor_created
    ON audit_logs(actor_user_id, created_at DESC, id DESC);

CREATE INDEX idx_audit_logs_target
    ON audit_logs(
        target_type,
        target_public_id,
        created_at DESC,
        id DESC
    );

CREATE INDEX idx_audit_logs_action_created
    ON audit_logs(action, created_at DESC, id DESC);

CREATE INDEX idx_audit_logs_correlation
    ON audit_logs(correlation_id);

-- Transactional Outbox:
-- insert domain data and outbox event in the same transaction, then publish
-- asynchronously to WebSocket, Redis or Push without losing committed events.
CREATE TABLE outbox_events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BINARY(16) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_public_id BINARY(16) NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    available_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    locked_at DATETIME(6) NULL,
    locked_by VARCHAR(100) NULL,
    published_at DATETIME(6) NULL,
    retry_count INT UNSIGNED NOT NULL DEFAULT 0,
    last_error VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_outbox_events PRIMARY KEY (id),
    CONSTRAINT uk_outbox_events_event_id UNIQUE (event_id),
    CONSTRAINT chk_outbox_events_status CHECK (
        status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_outbox_events_dispatch
    ON outbox_events(status, available_at, id);

CREATE INDEX idx_outbox_events_stale_lock
    ON outbox_events(status, locked_at, id);

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events(aggregate_type, aggregate_public_id, id);
