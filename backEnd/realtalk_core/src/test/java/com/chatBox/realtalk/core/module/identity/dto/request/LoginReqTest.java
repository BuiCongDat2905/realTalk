package com.chatBox.realtalk.core.module.identity.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test Bean Validation cho LoginReq.
 * Đối chiếu với đặc tả "Đăng nhập" mục 3 (LOGIN-VAL-001..008).
 *
 * LƯU Ý GAP:
 *  - Đặc tả yêu cầu field tên là "login" (chấp nhận username hoặc email);
 *    code hiện tại đang dùng field "username".
 *  - Đặc tả yêu cầu KHÔNG reject khoảng trắng ở DTO (trim ở service);
 *    code hiện tại có @Pattern(^\S+$) nên sẽ reject (xem test cuối).
 */
@DisplayName("LoginReq - Validation")
class LoginReqTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private LoginReq req(String username, String password) {
        return LoginReq.builder().username(username).password(password).build();
    }

    private Set<ConstraintViolation<LoginReq>> validate(LoginReq req) {
        return validator.validate(req);
    }

    @Nested
    @DisplayName("login/username bắt buộc")
    class LoginRequired {

        @Test
        @DisplayName("LOGIN-VAL-003: login = null → vi phạm")
        void nullLogin_shouldViolate() {
            assertThat(validate(req(null, "StrongPassword123!"))).isNotEmpty();
        }

        @Test
        @DisplayName("LOGIN-VAL-004: login chỉ toàn khoảng trắng → vi phạm")
        void blankLogin_shouldViolate() {
            assertThat(validate(req("   ", "StrongPassword123!"))).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("password bắt buộc")
    class PasswordRequired {

        @Test
        @DisplayName("LOGIN-VAL-005: password = null → vi phạm")
        void nullPassword_shouldViolate() {
            assertThat(validate(req("datcongh", null))).isNotEmpty();
        }

        @Test
        @DisplayName("LOGIN-VAL-006: password chỉ toàn khoảng trắng → vi phạm")
        void blankPassword_shouldViolate() {
            assertThat(validate(req("datcongh", "   "))).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Trường hợp hợp lệ")
    class ValidInput {

        @Test
        @DisplayName("login + password hợp lệ → không vi phạm")
        void validInput_shouldHaveNoViolations() {
            assertThat(validate(req("datcongh", "StrongPassword123!"))).isEmpty();
        }

        /**
         * GAP: đặc tả LOGIN-VAL-007 yêu cầu trim space đầu/cuối ở SERVICE,
         * KHÔNG reject ở tầng DTO. Hiện tại @Pattern("^\\S+$") reject mọi
         * khoảng trắng nên test này SẼ FAIL — báo hiệu cần bỏ @Pattern
         * và chỉ giữ @NotBlank.
         */
        @Test
        @DisplayName("LOGIN-VAL-007 (GAP): login có space đầu/cuối nên được trim, không reject ở DTO")
        void loginWithSurroundingSpaces_shouldNotBeRejected() {
            assertThat(validate(req("  datcongh  ", "StrongPassword123!"))).isEmpty();
        }
    }
}
