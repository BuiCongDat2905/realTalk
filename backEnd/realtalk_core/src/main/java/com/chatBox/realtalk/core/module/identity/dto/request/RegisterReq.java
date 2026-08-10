package com.chatBox.realtalk.core.module.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {

    @NotBlank(message = "Username không được để trống.")
    private String username;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email sai định dạng.")
    private String email;

    @NotBlank(message = "Password không được để trống.")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự.")
    private String password;
}
