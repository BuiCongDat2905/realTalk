package com.chatBox.realtalk.core.module.identity.dto.response;

import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRes {
    String username;
    String email;
    UserStatus status;
}
