-- ============================================================
-- realTalk - MySQL 8.x Database Schema
-- Generated from the project specification:
-- "TÀI LIỆU ĐẶC TẢ HỆ THỐNG CHAT REAL-TIME"
-- ============================================================

CREATE DATABASE IF NOT EXISTS realTalk_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE realTalk_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TABLE users (
                       id CHAR(36) NOT NULL,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       last_login_at DATETIME(6) NULL,
                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
                       version BIGINT NOT NULL DEFAULT 0,

                       PRIMARY KEY (id),
                       CONSTRAINT uk_users_username UNIQUE (username),
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
                       CONSTRAINT chk_users_status CHECK (
                           status IN ('ACTIVE', 'LOCKED', 'DISABLED', 'PENDING_VERIFY')
                           )
) ENGINE=InnoDB;

-- ============================================================
-- 2. USER PROFILES
-- ============================================================
CREATE TABLE user_profiles (
                               user_id CHAR(36) NOT NULL,
                               display_name VARCHAR(100) NOT NULL,
                               avatar_url VARCHAR(512) NULL,
                               bio VARCHAR(500) NULL,
                               last_seen_privacy VARCHAR(30) NOT NULL DEFAULT 'EVERYONE',
                               online_status_privacy VARCHAR(30) NOT NULL DEFAULT 'EVERYONE',
                               created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                               updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
                               version BIGINT NOT NULL DEFAULT 0,

                               PRIMARY KEY (user_id),
                               CONSTRAINT fk_user_profiles_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
                                       ON DELETE CASCADE,
                               CONSTRAINT chk_last_seen_privacy CHECK (
                                   last_seen_privacy IN ('EVERYONE', 'CONTACTS', 'NOBODY')
                                   ),
                               CONSTRAINT chk_online_status_privacy CHECK (
                                   online_status_privacy IN ('EVERYONE', 'CONTACTS', 'NOBODY')
                                   )
) ENGINE=InnoDB;

-- ============================================================
-- 3. OAUTH ACCOUNTS
-- ============================================================
CREATE TABLE oauth_accounts (
                                id CHAR(36) NOT NULL,
                                user_id CHAR(36) NOT NULL,
                                provider VARCHAR(30) NOT NULL,
                                provider_user_id VARCHAR(255) NOT NULL,
                                provider_email VARCHAR(255) NULL,
                                created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

                                PRIMARY KEY (id),
                                CONSTRAINT uk_oauth_provider_user UNIQUE (provider, provider_user_id),
                                CONSTRAINT fk_oauth_accounts_user
                                    FOREIGN KEY (user_id) REFERENCES users(id)
                                        ON DELETE CASCADE,
                                CONSTRAINT chk_oauth_provider CHECK (provider IN ('GOOGLE', 'FACEBOOK'))
) ENGINE=InnoDB;

CREATE INDEX idx_oauth_accounts_user_id
    ON oauth_accounts(user_id);

-- ============================================================
-- 4. USER SESSIONS
-- ============================================================
CREATE TABLE user_sessions (
                               id CHAR(36) NOT NULL,
                               user_id CHAR(36) NOT NULL,
                               token_hash VARCHAR(255) NOT NULL,
                               device_id VARCHAR(100) NULL,
                               device_name VARCHAR(255) NULL,
                               device_type VARCHAR(50) NULL,
                               user_agent VARCHAR(1000) NULL,
                               ip_address VARCHAR(45) NULL,
                               expires_at DATETIME(6) NOT NULL,
                               last_used_at DATETIME(6) NULL,
                               revoked_at DATETIME(6) NULL,
                               revoke_reason VARCHAR(255) NULL,
                               created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                               updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

                               PRIMARY KEY (id),
                               CONSTRAINT uk_user_sessions_token_hash UNIQUE (token_hash),
                               CONSTRAINT fk_user_sessions_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
                                       ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_user_sessions_user_id
    ON user_sessions(user_id);

CREATE INDEX idx_user_sessions_expires_at
    ON user_sessions(expires_at);

CREATE INDEX idx_user_sessions_user_revoked
    ON user_sessions(user_id, revoked_at);

-- ============================================================
-- 5. CONVERSATIONS
-- ============================================================
CREATE TABLE conversations (
                               id CHAR(36) NOT NULL,
                               type VARCHAR(20) NOT NULL,
                               name VARCHAR(150) NULL,
                               avatar_url VARCHAR(512) NULL,
                               direct_key VARCHAR(100) NULL,
                               owner_id CHAR(36) NULL,
                               last_message_id CHAR(36) NULL,
                               last_sequence BIGINT NOT NULL DEFAULT 0,
                               last_activity_at DATETIME(6) NULL,
                               created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                               updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
                               deleted_at DATETIME(6) NULL,
                               version BIGINT NOT NULL DEFAULT 0,

                               PRIMARY KEY (id),
                               CONSTRAINT uk_conversations_direct_key UNIQUE (direct_key),
                               CONSTRAINT fk_conversations_owner
                                   FOREIGN KEY (owner_id) REFERENCES users(id)
                                       ON DELETE SET NULL,
                               CONSTRAINT chk_conversations_type CHECK (type IN ('DIRECT', 'GROUP')),
                               CONSTRAINT chk_conversations_direct_key CHECK (
                                   (type = 'DIRECT' AND direct_key IS NOT NULL)
                                       OR
                                   (type = 'GROUP' AND direct_key IS NULL)
                                   )
) ENGINE=InnoDB;

-- ============================================================
-- 6. CONVERSATION MEMBERS
-- ============================================================
CREATE TABLE conversation_members (
                                      id CHAR(36) NOT NULL,
                                      conversation_id CHAR(36) NOT NULL,
                                      user_id CHAR(36) NOT NULL,
                                      role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                                      joined_sequence BIGINT NOT NULL DEFAULT 0,
                                      joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                      left_at DATETIME(6) NULL,
                                      archived_at DATETIME(6) NULL,
                                      muted_until DATETIME(6) NULL,
                                      notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                      last_activity_at DATETIME(6) NULL,
                                      created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                      updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
                                      version BIGINT NOT NULL DEFAULT 0,

                                      PRIMARY KEY (id),
                                      CONSTRAINT uk_conversation_members UNIQUE (conversation_id, user_id),
                                      CONSTRAINT fk_conversation_members_conversation
                                          FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                              ON DELETE CASCADE,
                                      CONSTRAINT fk_conversation_members_user
                                          FOREIGN KEY (user_id) REFERENCES users(id)
                                              ON DELETE CASCADE,
                                      CONSTRAINT chk_conversation_member_role CHECK (
                                          role IN ('OWNER', 'ADMIN', 'MEMBER')
                                          )
) ENGINE=InnoDB;

CREATE INDEX idx_conversation_members_user
    ON conversation_members(user_id, archived_at, last_activity_at);

CREATE INDEX idx_conversation_members_conversation
    ON conversation_members(conversation_id, left_at);

-- ============================================================
-- 7. MESSAGES
-- ============================================================
CREATE TABLE messages (
                          id CHAR(36) NOT NULL,
                          conversation_id CHAR(36) NOT NULL,
                          sender_id CHAR(36) NOT NULL,
                          sequence BIGINT NOT NULL,
                          client_message_id VARCHAR(64) NOT NULL,
                          type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
                          content_ciphertext LONGTEXT NULL,
                          search_content TEXT NULL,
                          reply_to_message_id CHAR(36) NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'SENT',
                          recalled_at DATETIME(6) NULL,
                          recalled_by CHAR(36) NULL,
                          created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                          updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
                          version BIGINT NOT NULL DEFAULT 0,

                          PRIMARY KEY (id),
                          CONSTRAINT uk_messages_conversation_sequence
                              UNIQUE (conversation_id, sequence),
                          CONSTRAINT uk_messages_sender_client_id
                              UNIQUE (conversation_id, sender_id, client_message_id),
                          CONSTRAINT fk_messages_conversation
                              FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_messages_sender
                              FOREIGN KEY (sender_id) REFERENCES users(id)
                                  ON DELETE RESTRICT,
                          CONSTRAINT fk_messages_reply_to
                              FOREIGN KEY (reply_to_message_id) REFERENCES messages(id)
                                  ON DELETE SET NULL,
                          CONSTRAINT fk_messages_recalled_by
                              FOREIGN KEY (recalled_by) REFERENCES users(id)
                                  ON DELETE SET NULL,
                          CONSTRAINT chk_messages_type CHECK (
                              type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM')
                              ),
                          CONSTRAINT chk_messages_status CHECK (
                              status IN ('SENT', 'DELIVERED', 'READ', 'FAILED')
                              )
) ENGINE=InnoDB;

CREATE INDEX idx_messages_conversation_sequence
    ON messages(conversation_id, sequence DESC);

CREATE INDEX idx_messages_sender
    ON messages(sender_id, created_at DESC);

CREATE INDEX idx_messages_reply_to
    ON messages(reply_to_message_id);

CREATE INDEX idx_messages_created_at
    ON messages(created_at DESC);

CREATE FULLTEXT INDEX ft_messages_search_content
    ON messages(search_content);

ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_last_message
        FOREIGN KEY (last_message_id) REFERENCES messages(id)
            ON DELETE SET NULL;

-- ============================================================
-- 8. ATTACHMENTS
-- ============================================================
CREATE TABLE attachments (
                             id CHAR(36) NOT NULL,
                             owner_id CHAR(36) NOT NULL,
                             storage_key VARCHAR(512) NOT NULL,
                             original_file_name VARCHAR(255) NOT NULL,
                             stored_file_name VARCHAR(255) NULL,
                             mime_type VARCHAR(150) NOT NULL,
                             file_size BIGINT NOT NULL,
                             checksum VARCHAR(128) NULL,
                             status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                             width INT NULL,
                             height INT NULL,
                             created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                             updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
                             completed_at DATETIME(6) NULL,
                             deleted_at DATETIME(6) NULL,
                             version BIGINT NOT NULL DEFAULT 0,

                             PRIMARY KEY (id),
                             CONSTRAINT uk_attachments_storage_key UNIQUE (storage_key),
                             CONSTRAINT fk_attachments_owner
                                 FOREIGN KEY (owner_id) REFERENCES users(id)
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
                             CONSTRAINT chk_attachments_file_size CHECK (file_size >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_attachments_owner
    ON attachments(owner_id, created_at DESC);

CREATE INDEX idx_attachments_status
    ON attachments(status);

-- ============================================================
-- 9. MESSAGE ATTACHMENTS
-- ============================================================
CREATE TABLE message_attachments (
                                     id CHAR(36) NOT NULL,
                                     message_id CHAR(36) NOT NULL,
                                     attachment_id CHAR(36) NOT NULL,
                                     display_order INT NOT NULL DEFAULT 0,
                                     created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                                     PRIMARY KEY (id),
                                     CONSTRAINT uk_message_attachments UNIQUE (message_id, attachment_id),
                                     CONSTRAINT fk_message_attachments_message
                                         FOREIGN KEY (message_id) REFERENCES messages(id)
                                             ON DELETE CASCADE,
                                     CONSTRAINT fk_message_attachments_attachment
                                         FOREIGN KEY (attachment_id) REFERENCES attachments(id)
                                             ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_message_attachments_message
    ON message_attachments(message_id, display_order);

CREATE INDEX idx_message_attachments_attachment
    ON message_attachments(attachment_id);

-- ============================================================
-- 10. MESSAGE USER STATES
-- ============================================================
CREATE TABLE message_user_states (
                                     id CHAR(36) NOT NULL,
                                     message_id CHAR(36) NOT NULL,
                                     user_id CHAR(36) NOT NULL,
                                     deleted_at DATETIME(6) NULL,
                                     starred_at DATETIME(6) NULL,
                                     created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                     updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

                                     PRIMARY KEY (id),
                                     CONSTRAINT uk_message_user_states UNIQUE (message_id, user_id),
                                     CONSTRAINT fk_message_user_states_message
                                         FOREIGN KEY (message_id) REFERENCES messages(id)
                                             ON DELETE CASCADE,
                                     CONSTRAINT fk_message_user_states_user
                                         FOREIGN KEY (user_id) REFERENCES users(id)
                                             ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_message_user_states_user_deleted
    ON message_user_states(user_id, deleted_at);

-- ============================================================
-- 11. CONVERSATION READ STATES
-- ============================================================
CREATE TABLE conversation_read_states (
                                          id CHAR(36) NOT NULL,
                                          conversation_id CHAR(36) NOT NULL,
                                          user_id CHAR(36) NOT NULL,
                                          last_delivered_sequence BIGINT NOT NULL DEFAULT 0,
                                          last_read_sequence BIGINT NOT NULL DEFAULT 0,
                                          delivered_at DATETIME(6) NULL,
                                          read_at DATETIME(6) NULL,
                                          created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                          updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
                                          version BIGINT NOT NULL DEFAULT 0,

                                          PRIMARY KEY (id),
                                          CONSTRAINT uk_conversation_read_states UNIQUE (conversation_id, user_id),
                                          CONSTRAINT fk_read_states_conversation
                                              FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                                  ON DELETE CASCADE,
                                          CONSTRAINT fk_read_states_user
                                              FOREIGN KEY (user_id) REFERENCES users(id)
                                                  ON DELETE CASCADE,
                                          CONSTRAINT chk_read_sequence CHECK (
                                              last_read_sequence >= 0
                                                  AND last_delivered_sequence >= 0
                                              )
) ENGINE=InnoDB;

CREATE INDEX idx_read_states_user
    ON conversation_read_states(user_id);

CREATE INDEX idx_read_states_conversation_sequence
    ON conversation_read_states(conversation_id, last_read_sequence);

-- ============================================================
-- 12. PUSH SUBSCRIPTIONS
-- ============================================================
CREATE TABLE push_subscriptions (
                                    id CHAR(36) NOT NULL,
                                    user_id CHAR(36) NOT NULL,
                                    endpoint TEXT NOT NULL,
                                    endpoint_hash VARCHAR(128) NOT NULL,
                                    p256dh_key VARCHAR(255) NOT NULL,
                                    auth_key VARCHAR(255) NOT NULL,
                                    user_agent VARCHAR(1000) NULL,
                                    device_name VARCHAR(255) NULL,
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    expires_at DATETIME(6) NULL,
                                    last_success_at DATETIME(6) NULL,
                                    last_failure_at DATETIME(6) NULL,
                                    failure_count INT NOT NULL DEFAULT 0,
                                    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

                                    PRIMARY KEY (id),
                                    CONSTRAINT uk_push_endpoint_hash UNIQUE (endpoint_hash),
                                    CONSTRAINT fk_push_subscriptions_user
                                        FOREIGN KEY (user_id) REFERENCES users(id)
                                            ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_push_subscriptions_user_active
    ON push_subscriptions(user_id, active);

-- ============================================================
-- 13. NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
                               id CHAR(36) NOT NULL,
                               user_id CHAR(36) NOT NULL,
                               type VARCHAR(50) NOT NULL,
                               title VARCHAR(255) NULL,
                               body VARCHAR(1000) NULL,
                               conversation_id CHAR(36) NULL,
                               message_id CHAR(36) NULL,
                               payload JSON NULL,
                               read_at DATETIME(6) NULL,
                               created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                               updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

                               PRIMARY KEY (id),
                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
                                       ON DELETE CASCADE,
                               CONSTRAINT fk_notifications_conversation
                                   FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                       ON DELETE CASCADE,
                               CONSTRAINT fk_notifications_message
                                   FOREIGN KEY (message_id) REFERENCES messages(id)
                                       ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_notifications_user_read_created
    ON notifications(user_id, read_at, created_at DESC);

CREATE INDEX idx_notifications_conversation
    ON notifications(conversation_id);

-- ============================================================
-- 14. AUDIT LOGS
-- ============================================================
CREATE TABLE audit_logs (
                            id CHAR(36) NOT NULL,
                            actor_id CHAR(36) NULL,
                            action VARCHAR(100) NOT NULL,
                            target_type VARCHAR(100) NULL,
                            target_id VARCHAR(100) NULL,
                            payload JSON NULL,
                            ip_address VARCHAR(45) NULL,
                            user_agent VARCHAR(1000) NULL,
                            correlation_id VARCHAR(64) NULL,
                            created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                            PRIMARY KEY (id),
                            CONSTRAINT fk_audit_logs_actor
                                FOREIGN KEY (actor_id) REFERENCES users(id)
                                    ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_audit_logs_actor_created
    ON audit_logs(actor_id, created_at DESC);

CREATE INDEX idx_audit_logs_target
    ON audit_logs(target_type, target_id);

CREATE INDEX idx_audit_logs_action_created
    ON audit_logs(action, created_at DESC);

CREATE INDEX idx_audit_logs_correlation
    ON audit_logs(correlation_id);

-- ============================================================
-- 15. USER BLOCKS
-- ============================================================
CREATE TABLE user_blocks (
                             id CHAR(36) NOT NULL,
                             blocker_id CHAR(36) NOT NULL,
                             blocked_id CHAR(36) NOT NULL,
                             created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                             PRIMARY KEY (id),
                             CONSTRAINT uk_user_blocks UNIQUE (blocker_id, blocked_id),
                             CONSTRAINT fk_user_blocks_blocker
                                 FOREIGN KEY (blocker_id) REFERENCES users(id)
                                     ON DELETE CASCADE,
                             CONSTRAINT fk_user_blocks_blocked
                                 FOREIGN KEY (blocked_id) REFERENCES users(id)
                                     ON DELETE CASCADE,
                             CONSTRAINT chk_user_blocks_not_self CHECK (blocker_id <> blocked_id)
) ENGINE=InnoDB;

CREATE INDEX idx_user_blocks_blocked
    ON user_blocks(blocked_id);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFY
-- ============================================================
SHOW TABLES;
