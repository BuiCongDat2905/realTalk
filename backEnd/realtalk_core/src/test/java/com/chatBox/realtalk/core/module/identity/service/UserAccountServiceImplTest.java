package com.chatBox.realtalk.core.module.identity.service;

import com.chatBox.realtalk.base.exception.AppException;
import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.enums.IdentityErrorCode;
import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import com.chatBox.realtalk.core.module.identity.repository.AuthenticationRepository;
import com.chatBox.realtalk.core.module.identity.repository.UserAccountRepository;
import com.chatBox.realtalk.core.module.identity.service.Impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test cho UserAccountServiceImpl.register().
 * <p>
 * Kiểm tra business logic: trùng email/username, lưu password (phát hiện plain text),
 * trả về đúng status, mock repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAccountServiceImpl - Register Service Tests")
class UserAccountServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterReq validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterReq();
        validRequest.setUsername("testuser");
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("Abc@123");
    }

    // ── Helper ──────────────────────────────────────────────────────

    private UserAccount mockSavedEntity() {
        return UserAccount.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("Abc@123") // ⚠️ plain text - sẽ fail test SEC-001
//                .status(UserStatus.ACTIVE) // ⚠️ ACTIVE - sẽ fail test SEC-006
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // SUCCESS CASES
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Đăng ký thành công")
    class SuccessfulRegistration {

        @Test
        @DisplayName("TC-BIZ-001: Đăng ký với dữ liệu hợp lệ → trả về RegisterRes")
        void shouldRegisterSuccessfully() {
            UserAccount saved = mockSavedEntity();
            when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
            when(userAccountRepository.existsByUsername(anyString())).thenReturn(false);
            when(userAccountRepository.save(any(UserAccount.class))).thenReturn(saved);

            RegisterRes result = authenticationService.register(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            verify(userAccountRepository).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("TC-BIZ-001b: Verify repository.save được gọi đúng 1 lần")
        void shouldCallSaveExactlyOnce() {
            when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
            when(userAccountRepository.existsByUsername(anyString())).thenReturn(false);
            when(userAccountRepository.save(any(UserAccount.class))).thenReturn(mockSavedEntity());

            authenticationService.register(validRequest);

            verify(userAccountRepository, times(1)).save(any(UserAccount.class));
        }

        @Test
        @DisplayName("TC-BIZ-001c: Entity được build với đúng username, email từ request")
        void shouldBuildEntityWithCorrectFields() {
            when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
            when(userAccountRepository.existsByUsername(anyString())).thenReturn(false);

            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            when(userAccountRepository.save(captor.capture())).thenReturn(mockSavedEntity());

            authenticationService.register(validRequest);

            UserAccount captured = captor.getValue();
            assertThat(captured.getUsername()).isEqualTo("testuser");
            assertThat(captured.getEmail()).isEqualTo("test@example.com");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DUPLICATE CASES
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Đăng ký với email/username đã tồn tại")
    class DuplicateRegistration {

        @Test
        @DisplayName("Email đã tồn tại thì throw REGISTER_EMAIL_EXISTS")
        void shouldThrowWhenEmailExists() {

            when(userAccountRepository.existsByEmail(validRequest.getEmail()))
                    .thenReturn(true);

            assertThatThrownBy(() -> authenticationService.register(validRequest))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appException = (AppException) ex;

                        assertThat(appException.getErrorCode())
                                .isEqualTo(IdentityErrorCode.REGISTER_EMAIL_EXISTS);
                    });

            verify(userAccountRepository, never())
                    .save(any(UserAccount.class));
        }

        @Test
        @DisplayName("Username đã tồn tại thì throw REGISTER_USERNAME_EXISTS")
        void shouldThrowWhenUsernameExists() {

            when(userAccountRepository.existsByUsername(validRequest.getUsername()))
                    .thenReturn(true);

            assertThatThrownBy(() -> authenticationService.register(validRequest))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appException = (AppException) ex;

                        assertThat(appException.getErrorCode())
                                .isEqualTo(IdentityErrorCode.REGISTER_USERNAME_EXISTS);
                    });

            verify(userAccountRepository, never())
                    .save(any(UserAccount.class));
        }

    // ═══════════════════════════════════════════════════════════════
    // SECURITY TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("🚨 Kiểm tra bảo mật (SEC-001, SEC-006)")
    class SecurityTests {

        @Test
        @DisplayName("SEC-001 (FAIL): Password được lưu dạng PLAIN TEXT — phải là hash")
        void passwordMustBeHashedNotPlainText() {
            when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
            when(userAccountRepository.existsByUsername(anyString())).thenReturn(false);

            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            when(userAccountRepository.save(captor.capture())).thenReturn(mockSavedEntity());

            authenticationService.register(validRequest);

            UserAccount saved = captor.getValue();
            String rawPassword = validRequest.getPassword();

            // ⚠️ TEST NÀY SẼ FAIL vì password được lưu plain text
            // Khi fix: assertThat(saved.getPasswordHash()).isNotEqualTo(rawPassword);
            //          assertThat(saved.getPasswordHash()).startsWith("$2a$"); // BCrypt format
            assertThat(saved.getPasswordHash())
                    .as("CRITICAL: Password đang bị lưu plain text! Phải được hash trước khi lưu.")
                    .isNotEqualTo(rawPassword);
        }

        @Test
        @DisplayName("SEC-006 (FAIL): Status mặc định là ACTIVE — phải là PENDING_VERIFY")
        void newUserShouldHavePendingVerifyStatus() {
            when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
            when(userAccountRepository.existsByUsername(anyString())).thenReturn(false);

            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            when(userAccountRepository.save(captor.capture())).thenReturn(mockSavedEntity());

            authenticationService.register(validRequest);

            UserAccount saved = captor.getValue();

            // ⚠️ TEST NÀY SẼ FAIL vì entity default là ACTIVE
            // Khi fix: assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING_VERIFY);
            assertThat(saved.getStatus())
                    .as("SEC-006: Status phải là PENDING_VERIFY, không phải ACTIVE ngay sau đăng ký.")
                    .isEqualTo(UserStatus.PENDING_VERIFY);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // RESPONSE TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Kiểm tra Response")
    class ResponseTests {

        @Test
        @DisplayName("TC-RES-004,005: Response chứa đúng username và email")
        void responseShouldContainCorrectUsernameAndEmail() {
            when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
            when(userAccountRepository.existsByUsername(anyString())).thenReturn(false);
            when(userAccountRepository.save(any(UserAccount.class))).thenReturn(mockSavedEntity());

            RegisterRes result = authenticationService.register(validRequest);

            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("TC-RES-007: Response KHÔNG chứa password")
        void responseShouldNotContainPassword() {
            when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
            when(userAccountRepository.existsByUsername(anyString())).thenReturn(false);
            when(userAccountRepository.save(any(UserAccount.class))).thenReturn(mockSavedEntity());

            RegisterRes result = authenticationService.register(validRequest);

            // RegisterRes chỉ có username, email, status → không có password field
            // Test này luôn pass do cấu trúc DTO
            assertThat(result.getClass().getDeclaredFields())
                    .noneMatch(f -> f.getName().toLowerCase().contains("password"));
        }
    }
}
}
