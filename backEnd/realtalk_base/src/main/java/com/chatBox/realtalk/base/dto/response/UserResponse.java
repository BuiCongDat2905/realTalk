package com.chatBox.realtalk.base.dto.response;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String avatarKey,
        String phone,
        String bio,
        String role,
        String status,
        boolean online,
        Instant createdAt,
        Instant updatedAt
) {
}
