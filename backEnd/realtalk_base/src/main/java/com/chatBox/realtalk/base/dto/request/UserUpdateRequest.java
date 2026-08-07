package com.chatBox.realtalk.base.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 100) String displayName,
        @Size(max = 500) String avatarUrl,
        @Size(max = 20) String phone,
        String bio
) {
}
