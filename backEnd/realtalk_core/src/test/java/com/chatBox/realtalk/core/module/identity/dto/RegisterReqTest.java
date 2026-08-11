package com.chatBox.realtalk.core.module.identity.dto;

import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test validation annotations trên RegisterReq DTO.
 * <p>
 * Bao phủ: @NotBlank, @Size, @Email, @Pattern cho username, email, password.
 */
@DisplayName("RegisterReq Validation Tests")
class RegisterReqTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // ── Helper methods ──────────────────────────────────────────────

    private Set<String> validate(RegisterReq req) {
        return validator.validate(req).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    private RegisterReq validRequest() {
        RegisterReq req = new RegisterReq();
        req.setUsername("testuser");
        req.setEmail("test@example.com");
        req.setPassword("Abc@123");
        return req;
    }

    // ── TC-VAL-001: Valid request ───────────────────────────────────

    @Test
    @DisplayName("TC-VAL-001: Request hợp lệ hoàn toàn → không có lỗi validation")
    void shouldPassWithValidRequest() {
        Set<String> violations = validate(validRequest());
        assertThat(violations).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════
    // USERNAME VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Username Validation")
    class UsernameValidation {

        @Test
        @DisplayName("TC-USER-001: Username quá ngắn (2 ký tự) → lỗi")
        void shouldRejectTooShortUsername() {
            RegisterReq req = validRequest();
            req.setUsername("ab");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("ít nhất 3 ký tự"));
        }

        @Test
        @DisplayName("TC-USER-002: Username đúng min (3 ký tự) → hợp lệ")
        void shouldAcceptMinLengthUsername() {
            RegisterReq req = validRequest();
            req.setUsername("abc");
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-USER-003: Username đúng max (50 ký tự) → hợp lệ")
        void shouldAcceptMaxLengthUsername() {
            RegisterReq req = validRequest();
            req.setUsername("a".repeat(50));
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-USER-004: Username vượt max (51 ký tự) → lỗi")
        void shouldRejectTooLongUsername() {
            RegisterReq req = validRequest();
            req.setUsername("a".repeat(51));
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("không được vượt quá 50"));
        }

        @Test
        @DisplayName("TC-USER-005: Username chứa khoảng trắng → lỗi")
        void shouldRejectUsernameWithSpace() {
            RegisterReq req = validRequest();
            req.setUsername("user name");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("khoảng trắng"));
        }

        @Test
        @DisplayName("TC-USER-006: Username chứa ký tự đặc biệt @ → lỗi")
        void shouldRejectUsernameWithSpecialChar() {
            RegisterReq req = validRequest();
            req.setUsername("user@name");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("chỉ được chứa chữ cái"));
        }

        @Test
        @DisplayName("TC-USER-007: Username chỉ có số → hợp lệ")
        void shouldAcceptNumericUsername() {
            RegisterReq req = validRequest();
            req.setUsername("12345");
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-USER-008: Username có dấu chấm → hợp lệ")
        void shouldAcceptDotInUsername() {
            RegisterReq req = validRequest();
            req.setUsername("user.name");
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-USER-009: Username có dấu gạch dưới → hợp lệ")
        void shouldAcceptUnderscoreInUsername() {
            RegisterReq req = validRequest();
            req.setUsername("user_name");
            assertThat(validate(req)).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("TC-USER-010-012: Username null/rỗng/blank → lỗi NotBlank")
        void shouldRejectBlankUsername(String username) {
            RegisterReq req = validRequest();
            req.setUsername(username);
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("không được để trống"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EMAIL VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Email Validation")
    class EmailValidation {

        @Test
        @DisplayName("TC-EMAIL-001: Email hợp lệ → không lỗi")
        void shouldAcceptValidEmail() {
            RegisterReq req = validRequest();
            req.setEmail("test@example.com");
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-EMAIL-002: Email thiếu @ → lỗi")
        void shouldRejectEmailWithoutAt() {
            RegisterReq req = validRequest();
            req.setEmail("testexample.com");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("sai định dạng"));
        }

        @Test
        @DisplayName("TC-EMAIL-003: Email thiếu domain → lỗi")
        void shouldRejectEmailWithoutDomain() {
            RegisterReq req = validRequest();
            req.setEmail("test@");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("sai định dạng"));
        }

        @Test
        @DisplayName("TC-EMAIL-004: Email thiếu local-part → lỗi")
        void shouldRejectEmailWithoutLocalPart() {
            RegisterReq req = validRequest();
            req.setEmail("@example.com");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("sai định dạng"));
        }

        @Test
        @DisplayName("TC-EMAIL-005: Email chứa khoảng trắng → lỗi")
        void shouldRejectEmailWithSpace() {
            RegisterReq req = validRequest();
            req.setEmail("test @example.com");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("khoảng trắng"));
        }

        @Test
        @DisplayName("TC-EMAIL-006: Email > 254 ký tự → lỗi")
        void shouldRejectTooLongEmail() {
            RegisterReq req = validRequest();
            req.setEmail("a".repeat(250) + "@b.com");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("không được vượt quá 254"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("TC-EMAIL-007-008: Email null/rỗng/blank → lỗi NotBlank")
        void shouldRejectBlankEmail(String email) {
            RegisterReq req = validRequest();
            req.setEmail(email);
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("không được để trống"));
        }

        @Test
        @DisplayName("TC-EMAIL-009: Email có dấu + (Gmail alias) → hợp lệ")
        void shouldAcceptGmailAlias() {
            RegisterReq req = validRequest();
            req.setEmail("test+alias@gmail.com");
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-EMAIL-010: Email subdomain → hợp lệ")
        void shouldAcceptSubdomainEmail() {
            RegisterReq req = validRequest();
            req.setEmail("test@mail.example.co.uk");
            assertThat(validate(req)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PASSWORD VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Password Validation")
    class PasswordValidation {

        @Test
        @DisplayName("TC-PASS-001: Password hợp lệ (Abc@123) → không lỗi")
        void shouldAcceptValidPassword() {
            RegisterReq req = validRequest();
            req.setPassword("Abc@123");
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-PASS-002: Password < 6 ký tự → lỗi")
        void shouldRejectTooShortPassword() {
            RegisterReq req = validRequest();
            req.setPassword("Ab@1");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("ít nhất 6 ký tự"));
        }

        @Test
        @DisplayName("TC-PASS-003: Password đúng min (6 ký tự) → hợp lệ")
        void shouldAcceptMinLengthPassword() {
            RegisterReq req = validRequest();
            req.setPassword("Abc@12");
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-PASS-005: Password thiếu chữ hoa → lỗi")
        void shouldRejectPasswordWithoutUppercase() {
            RegisterReq req = validRequest();
            req.setPassword("abc@123");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("viết hoa"));
        }

        @Test
        @DisplayName("TC-PASS-006: Password thiếu chữ thường → lỗi")
        void shouldRejectPasswordWithoutLowercase() {
            RegisterReq req = validRequest();
            req.setPassword("ABC@123");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("ít nhất 1 chữ cái"));
        }

        @Test
        @DisplayName("TC-PASS-007: Password thiếu số → lỗi")
        void shouldRejectPasswordWithoutDigit() {
            RegisterReq req = validRequest();
            req.setPassword("Abc@def");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("chữ số"));
        }

        @Test
        @DisplayName("TC-PASS-008: Password thiếu ký tự đặc biệt → lỗi")
        void shouldRejectPasswordWithoutSpecialChar() {
            RegisterReq req = validRequest();
            req.setPassword("Abc1234");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("ký tự đặc biệt"));
        }

        @Test
        @DisplayName("TC-PASS-009: Password chứa khoảng trắng → lỗi")
        void shouldRejectPasswordWithSpace() {
            RegisterReq req = validRequest();
            req.setPassword("Abc @123");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("khoảng trắng"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("TC-PASS-010-012: Password null/rỗng/blank → lỗi NotBlank")
        void shouldRejectBlankPassword(String password) {
            RegisterReq req = validRequest();
            req.setPassword(password);
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("không được để trống"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // COMBINED / EDGE CASES
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases & Combined Errors")
    class EdgeCases {

        @Test
        @DisplayName("TC-EDGE-001: Tất cả trường null → nhiều lỗi validation")
        void shouldReportAllErrorsWhenAllFieldsNull() {
            RegisterReq req = new RegisterReq();
            Set<String> violations = validate(req);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(3); // 3 @NotBlank
            assertThat(violations).anyMatch(m -> m.contains("Username") || m.contains("không được để trống"));
            assertThat(violations).anyMatch(m -> m.contains("Email") || m.contains("không được để trống"));
            assertThat(violations).anyMatch(m -> m.contains("Password") || m.contains("không được để trống"));
        }

        @Test
        @DisplayName("TC-EDGE-002: Username và password không hợp lệ → báo cả 2 lỗi")
        void shouldReportMultipleFieldErrors() {
            RegisterReq req = validRequest();
            req.setUsername("ab");          // quá ngắn
            req.setPassword("abc");         // thiếu hoa, số, đặc biệt
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("ít nhất 3 ký tự"));
            assertThat(violations.stream().filter(m -> m.contains("mật khẩu") || m.contains("Password")).count())
                    .isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("TC-EDGE-003: Password đúng max (64 ký tự) → hợp lệ")
        void shouldAcceptMaxLengthPassword() {
            RegisterReq req = validRequest();
            // 64 ký tự: 1 hoa + 1 số + 1 đặc biệt + 61 chữ thường
            req.setPassword("A" + "b".repeat(60) + "1@");
            assertThat(req.getPassword()).hasSize(64);
            assertThat(validate(req)).isEmpty();
        }

        @Test
        @DisplayName("TC-EDGE-004: Password > 64 ký tự → lỗi")
        void shouldRejectPasswordOver64Chars() {
            RegisterReq req = validRequest();
            req.setPassword("A" + "b".repeat(61) + "1@");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("không được vượt quá 64"));
        }

        @Test
        @DisplayName("TC-EDGE-005: Username chứa ký tự Unicode → lỗi")
        void shouldRejectUnicodeUsername() {
            RegisterReq req = validRequest();
            req.setUsername("người_dùng");
            Set<String> violations = validate(req);
            assertThat(violations).anyMatch(m -> m.contains("chỉ được chứa chữ cái"));
        }
    }
}
