package com.chatBox.realtalk.core.module.identity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE("active"),
    LOCKED("locked"),
    DISABLED("disabled"),
    PENDING_VERIFY("pending_verify"),
    ;

    private final String value;
}
