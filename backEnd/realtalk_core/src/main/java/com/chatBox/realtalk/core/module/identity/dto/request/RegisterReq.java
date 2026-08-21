package com.chatBox.realtalk.core.module.identity.dto.request;

import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.chatBox.realtalk.core.module.identity.validation.RegisterVali.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {

    @NotBlank(message = "Username không được để trống.")
    @Size(min = USERNAME_MIN_LENGTH, message = "Username phải có ít nhất 3 ký tự.")
    @Size(max = USERNAME_MAX_LENGTH, message = "Username không được vượt quá 50 ký tự.")
    @Pattern(
            regexp = "^\\S+$",
            message = "Username chứa khoảng trắng."
    )
    @Pattern(
            regexp = "^[A-Za-z0-9._]+$",
            message = "Username chỉ được chứa chữ cái, số, dấu chấm và dấu gạch dưới."
    )
    private String username;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email sai định dạng.")
    @Size(max = EMAIL_MAX_LENGTH, message = "Email không được vượt quá 254 ký tự.")
    @Pattern(
            regexp = "^\\S+$",
            message = "Email chứa khoảng trắng."
    )
    private String email;

    @NotBlank(message = "Password không được để trống.")
    @Size(min = PASSWORD_MIN_LENGTH, message = "Password phải có ít nhất 6 ký tự.")
    @Size(max = PASSWORD_MAX_LENGTH, message = "Mật khẩu không được vượt quá 64 ký tự.")
    @Pattern(
            regexp = "^\\S+$",
            message = "Password chứa khoảng trắng."
    )
    @Pattern(
            regexp = ".*[A-Za-z].*",
            message = "Mật khẩu phải chứa ít nhất 1 chữ cái."
    )
    @Pattern(
            regexp = ".*[A-Z].*",
            message = "Mật khẩu phải chứa ít nhất 1 chữ cái viết hoa."
    )
    @Pattern(
            regexp = ".*\\d.*",
            message = "Mật khẩu phải chứa ít nhất 1 chữ số."
    )
    @Pattern(
            regexp = ".*[^A-Za-z0-9\\s].*",
            message = "Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt."
    )
    private String password;
//    @NotNull(message = "Status không được để trống")
//    private UserStatus status;
}
