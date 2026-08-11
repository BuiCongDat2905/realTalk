package com.chatBox.realtalk.core.module.identity.validation;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterVali {
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int EMAIL_MAX_LENGTH = 254;
    public static final int PASSWORD_MAX_LENGTH = 64;
    public static final int PASSWORD_MIN_LENGTH = 6;
}
