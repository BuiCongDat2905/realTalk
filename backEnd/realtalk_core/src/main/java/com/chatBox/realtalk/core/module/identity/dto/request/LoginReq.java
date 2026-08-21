package com.chatBox.realtalk.core.module.identity.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginReq {

    @NotBlank(message = "Username không được để trống.")
    @Pattern(
            regexp = "^\\S+$",
            message = "Username chứa khoảng trắng."
    )
    private String username;

    @NotBlank(message = "Password không được để trống.")
    private String password;

}
