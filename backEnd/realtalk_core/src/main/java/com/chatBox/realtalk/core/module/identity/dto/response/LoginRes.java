package com.chatBox.realtalk.core.module.identity.dto.response;

import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRes {
    String token;
    UUID publicId;
    String username;
}
