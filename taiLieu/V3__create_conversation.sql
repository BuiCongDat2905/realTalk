-- ============================================================================
-- RealTalk - V2: Conversations, direct-conversation uniqueness and membership
-- ============================================================================

CREATE TABLE conversations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(150) NULL,
    avatar_key VARCHAR(512) NULL,
    created_by_user_id BIGINT UNSIGNED NOT NULL,

    -- Denormalized pointers maintained in the same transaction as message insert.
    -- No FK on last_message_id intentionally; see README_SCHEMA_DESIGN.md.
    last_message_id BIGINT UNSIGNED NULL,
    last_sequence BIGINT UNSIGNED NOT NULL DEFAULT 0,
    last_activity_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,

    CONSTRAINT pk_conversations PRIMARY KEY (id),
    CONSTRAINT uk_conversations_public_id UNIQUE (public_id),
    CONSTRAINT fk_conversations_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_conversations_type CHECK (
        type IN ('DIRECT', 'GROUP')
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_conversations_last_activity
    ON conversations(last_activity_at DESC, id DESC);

-- One-to-one extension used only by DIRECT conversations.
-- user_low_id and user_high_id must always be sorted before insert.
CREATE TABLE direct_conversations (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_low_id BIGINT UNSIGNED NOT NULL,
    user_high_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_direct_conversations PRIMARY KEY (conversation_id),
    CONSTRAINT uk_direct_conversations_pair
        UNIQUE (user_low_id, user_high_id),
    CONSTRAINT fk_direct_conversations_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_direct_conversations_user_low
        FOREIGN KEY (user_low_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_direct_conversations_user_high
        FOREIGN KEY (user_high_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_direct_conversations_order CHECK (
        user_low_id < user_high_id
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE conversation_members (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    membership_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Sequence cursors are aggregated per user, not per device.
    joined_sequence BIGINT UNSIGNED NOT NULL DEFAULT 0,
    last_delivered_sequence BIGINT UNSIGNED NOT NULL DEFAULT 0,
    last_read_sequence BIGINT UNSIGNED NOT NULL DEFAULT 0,
    delivered_at DATETIME(6) NULL,
    read_at DATETIME(6) NULL,

    notification_level VARCHAR(20) NOT NULL DEFAULT 'ALL',
    muted_until DATETIME(6) NULL,
    archived_at DATETIME(6) NULL,
    pinned_at DATETIME(6) NULL,

    joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    left_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,

    CONSTRAINT pk_conversation_members
        PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_conversation_members_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_conversation_members_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_conversation_members_role CHECK (
        role IN ('OWNER', 'ADMIN', 'MEMBER')
    ),
    CONSTRAINT chk_conversation_members_status CHECK (
        membership_status IN ('ACTIVE', 'LEFT', 'REMOVED')
    ),
    CONSTRAINT chk_conversation_members_notification CHECK (
        notification_level IN ('ALL', 'MENTIONS', 'NONE')
    ),
    CONSTRAINT chk_conversation_members_sequences CHECK (
        last_delivered_sequence >= last_read_sequence
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Fetch all active/archived conversations belonging to a user.
CREATE INDEX idx_conversation_members_user_inbox
    ON conversation_members(
        user_id,
        membership_status,
        archived_at,
        pinned_at DESC,
        conversation_id
    );

-- Fetch active members of one conversation.
CREATE INDEX idx_conversation_members_active
    ON conversation_members(
        conversation_id,
        membership_status,
        user_id
    );

-- Find conversations that still have unread data for one user.
CREATE INDEX idx_conversation_members_user_read_cursor
    ON conversation_members(
        user_id,
        membership_status,
        last_read_sequence,
        conversation_id
    );
