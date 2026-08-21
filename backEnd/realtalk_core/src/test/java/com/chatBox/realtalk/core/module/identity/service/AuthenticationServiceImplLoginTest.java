package com.chatBox.realtalk.core.module.identity.service;

import com.chatBox.realtalk.core.module.identity.dto.request.LoginReq;
import com.chatBox.realtalk.core.module.identity.dto.response.LoginRes;
import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.enums.IdentityErrorCode;
import com.chatBox.realtalk.core.module.identity.enums.UserRole;
import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import com.chatBox.realtalk.core.module.identity.exception.AppException;
import com.chatBox.realtalk.core.module.identity.repository.UserAccountRepository;
import com.chatBox.realtalk.core.module.identity.repository.UserSessionsRepository;
import com.chatBox.realtalk.core.module.identity.service.Impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho AuthenticationServiceImpl.login().
 * Đối chiếu với đặc tả "Đăng nhập" mục 11 (T01..T29) và mục 4 (LOGIN-BIZ-*).
 *
 * GHI CHÚ GAP (test sẽ FAIL để lộ lỗi cần sửa):
 *  - T13/T14: code hiện chưa check UserStatus (LOCKED/DISABLED) nên 2 test status sẽ FAIL.
 *  - T10+T11: code đang dùng 2 error code khác nhau cho "user không tồn tại" và
 *    "sai password" — vi phạm chống user enumeration nên test "cùng error code" sẽ FAIL.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl.login - Đăng nhập")
class AuthenticationServiceImplLoginTest {

    private static final String SIGNER_KEY =
            "0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz";

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private UserSessionsRepository userSessionsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        // @Value field không được Mockito inject → gán thủ công cho việc ký token.
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", SIGNER_KEY);
    }

    private UserAccount account(String username, String email, UserStatus status, String passwordHash) {
        return UserAccount.builder()
                .username(username)
                .email(email)
                .status(status)
                .systemRole(UserRole.USER)
                .passwordHash(passwordHash)
                .publicId(UUID.randomUUID())
                .build();
    }

    private LoginReq loginReq(String login, String password) {
        // NOTE: đặc tả dùng field "login"; code hiện dùng "username".
        return LoginReq.builder().username(login).password(password).build();
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("T01: username + password đúng + ACTIVE → trả token")
        void usernameAndPasswordCorrect_shouldReturnToken() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.ACTIVE, "$2a$10$hash");
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("StrongPassword123!", "$2a$10$hash")).thenReturn(true);

            LoginRes res = authenticationService.login(loginReq("datcongh", "StrongPassword123!"));

            assertThat(res.getUsername()).isEqualTo("datcongh");
            assertThat(res.getPublicId()).isEqualTo(user.getPublicId());
            assertThat(res.getToken()).isNotBlank();
        }

        @Test
        @DisplayName("T02: email + password đúng → trả token")
        void emailAndPasswordCorrect_shouldReturnToken() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.ACTIVE, "$2a$10$hash");
            when(userAccountRepository.existsByEmail("datcongh@example.com")).thenReturn(true);
            when(userAccountRepository.findByEmail("datcongh@example.com")).thenReturn(user);
            when(passwordEncoder.matches("StrongPassword123!", "$2a$10$hash")).thenReturn(true);

            LoginRes res = authenticationService.login(loginReq("datcongh@example.com", "StrongPassword123!"));

            assertThat(res.getUsername()).isEqualTo("datcongh");
            assertThat(res.getToken()).isNotBlank();
        }

        @Test
        @DisplayName("T03: login có khoảng trắng đầu/cuối → trim trước khi lookup")
        void login_shouldTrimBeforeLookup() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.ACTIVE, "$2a$10$hash");
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(eq("StrongPassword123!"), any())).thenReturn(true);

            authenticationService.login(loginReq("  datcongh  ", "StrongPassword123!"));

            verify(userAccountRepository).findByUsername("datcongh");
        }
    }

    @Nested
    @DisplayName("Thất bại credential")
    class InvalidCredentials {

        @Test
        @DisplayName("T10: user không tồn tại → ném ngoại lệ")
        void userNotFound_shouldThrow() {
            when(userAccountRepository.existsByUsername("ghost")).thenReturn(false);
            when(userAccountRepository.existsByEmail("ghost")).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.login(loginReq("ghost", "whatever")))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(IdentityErrorCode.LOGIN_USERNAME_NOT_EXISTS));
        }

        @Test
        @DisplayName("T11: user tồn tại + password sai → ném ngoại lệ")
        void wrongPassword_shouldThrow() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.ACTIVE, "$2a$10$hash");
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", "$2a$10$hash")).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.login(loginReq("datcongh", "wrong")))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(IdentityErrorCode.LOGIN_PASSWORD_NOT_EXITS));
        }

        /**
         * GAP (user enumeration): đặc tả yêu cầu "user không tồn tại" và
         * "password sai" PHẢI trả cùng error code INVALID_CREDENTIALS.
         * Code hiện dùng 2 code khác nhau → test này FAIL.
         */
        @Test
        @DisplayName("T10+T11 (GAP): user không tồn tại và password sai PHẢI dùng cùng error code")
        void userNotFoundAndWrongPassword_shouldUseSameErrorCode() {
            when(userAccountRepository.existsByUsername("ghost")).thenReturn(false);
            when(userAccountRepository.existsByEmail("ghost")).thenReturn(false);
            AppException notFound = catchThrowableOfType(
                    () -> authenticationService.login(loginReq("ghost", "whatever")),
                    AppException.class);

            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.ACTIVE, "$2a$10$hash");
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", "$2a$10$hash")).thenReturn(false);
            AppException wrongPw = catchThrowableOfType(
                    () -> authenticationService.login(loginReq("datcongh", "wrong")),
                    AppException.class);

            assertThat(notFound).isNotNull();
            assertThat(wrongPw).isNotNull();
            assertThat(notFound.getErrorCode()).isEqualTo(wrongPw.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Trạng thái tài khoản")
    class AccountStatus {

        /**
         * GAP: đặc tả T13 — LOCKED + password đúng phải bị từ chối (ACCOUNT_LOCKED).
         * Code hiện KHÔNG check status nên login vẫn thành công → test FAIL.
         */
        @Test
        @DisplayName("T13 (GAP): LOCKED + password đúng → PHẢI bị từ chối")
        void lockedAccount_shouldBeRejected() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.LOCKED, "$2a$10$hash");
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("StrongPassword123!", "$2a$10$hash")).thenReturn(true);

            assertThatThrownBy(() -> authenticationService.login(loginReq("datcongh", "StrongPassword123!")))
                    .isInstanceOf(AppException.class);
        }

        /**
         * GAP: đặc tả T14 — DISABLED + password đúng phải bị từ chối (ACCOUNT_DISABLED).
         * Code hiện KHÔNG check status → test FAIL.
         */
        @Test
        @DisplayName("T14 (GAP): DISABLED + password đúng → PHẢI bị từ chối")
        void disabledAccount_shouldBeRejected() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.DISABLED, "$2a$10$hash");
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("StrongPassword123!", "$2a$10$hash")).thenReturn(true);

            assertThatThrownBy(() -> authenticationService.login(loginReq("datcongh", "StrongPassword123!")))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("OAuth-only và password đặc biệt")
    class EdgeCases {

        @Test
        @DisplayName("T12: password_hash = null (OAuth-only) → không NPE, trả lỗi credential")
        void oauthOnlyAccount_nullHash_shouldNotThrowNpe() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.ACTIVE, null);
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            // passwordEncoder là mock → matches trả false → ném ngoại lệ, không NPE.

            assertThatThrownBy(() -> authenticationService.login(loginReq("datcongh", "StrongPassword123!")))
                    .isInstanceOf(AppException.class);
        }

        @Test
        @DisplayName("T29: password không bị trim/normalize trước khi verify")
        void password_shouldNotBeTrimmed() {
            UserAccount user = account("datcongh", "datcongh@example.com", UserStatus.ACTIVE, "$2a$10$hash");
            when(userAccountRepository.existsByUsername("datcongh")).thenReturn(true);
            when(userAccountRepository.findByUsername("datcongh")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(eq(" pass with spaces "), any())).thenReturn(true);

            authenticationService.login(loginReq("datcongh", " pass with spaces "));

            verify(passwordEncoder).matches(" pass with spaces ", "$2a$10$hash");
        }
    }
}
