package com.chatBox.realtalk.core.module.identity.controller;

import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import com.chatBox.realtalk.core.module.identity.service.AuthenticationService;
import com.chatBox.realtalk.core.module.identity.service.UserAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test cho AuthController endpoint POST /api/v1/auth/register.
 * <p>
 * Sử dụng MockMvc standalone setup (không dùng @WebMvcTest vì Spring Boot 4.1.0
 * đã thay đổi package structure).
 * Mock UserAccountService để kiểm soát business logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - POST /api/v1/auth/register Integration Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private RegisterReq validRequest;

    @BeforeEach
    void setUp() {
        // Standalone setup with GlobalExceptionHandler
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.chatBox.realtalk.base.exception.GlobalExceptionHandler())
                .build();

        validRequest = new RegisterReq();
        validRequest.setUsername("testuser");
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("Abc@123");

        RegisterRes mockResponse = RegisterRes.builder()
                .username("testuser")
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .build();
        when(authenticationService.register(any(RegisterReq.class))).thenReturn(mockResponse);
    }

    @Nested
    @DisplayName("Đăng ký thành công")
    class SuccessfulRegistration {

        @Test
        @DisplayName("TC-200-001: Request hợp lệ → 200 OK + ApiResponse wrapper")
        void shouldReturn200WithApiResponse() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Đăng ký thành công."))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("TC-200-002: Response chứa data.username và data.email")
        void shouldReturnCorrectUserData() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @DisplayName("TC-200-003: Service được gọi đúng 1 lần")
        void shouldCallServiceOnce() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            verify(authenticationService, times(1)).register(any(RegisterReq.class));
        }
    }

    @Nested
    @DisplayName("Lỗi validation input")
    class ValidationErrors {

        @Test
        @DisplayName("TC-400-001: Body rỗng {} → 400 Bad Request")
        void shouldReturn400ForEmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(authenticationService, never()).register(any());
        }

        @Test
        @DisplayName("TC-400-002: Body rỗng → 400 Bad Request")
        void shouldReturn400ForMissingBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("TC-400-003: Body không phải JSON → 400")
        void shouldReturn400ForNonJsonBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("plain text not json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("TC-400-004: Thiếu username → 400")
        void shouldReturn400WhenUsernameMissing() throws Exception {
            validRequest.setUsername(null);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("username"));
        }

        @Test
        @DisplayName("TC-400-005: Thiếu email → 400")
        void shouldReturn400WhenEmailMissing() throws Exception {
            validRequest.setEmail(null);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("email"));
        }

        @Test
        @DisplayName("TC-400-006: Thiếu password → 400")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            validRequest.setPassword(null);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("password"));
        }

        @Test
        @DisplayName("TC-400-007: Username quá ngắn → 400")
        void shouldReturn400ForTooShortUsername() throws Exception {
            validRequest.setUsername("ab");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("username"));
        }

        @Test
        @DisplayName("TC-400-008: Email sai định dạng → 400")
        void shouldReturn400ForInvalidEmail() throws Exception {
            validRequest.setEmail("not-an-email");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("email"));
        }

        @Test
        @DisplayName("TC-400-009: Password yếu → 400")
        void shouldReturn400ForWeakPassword() throws Exception {
            validRequest.setPassword("Abc1234");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("password"));
        }
    }

    @Nested
    @DisplayName("Phương thức HTTP không hỗ trợ")
    class MethodNotAllowed {

        @Test
        @DisplayName("TC-405-001: GET /register → 405")
        void shouldReturn405ForGetRequest() throws Exception {
            mockMvc.perform(get("/api/v1/auth/register"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("TC-405-002: PUT /register → 405")
        void shouldReturn405ForPutRequest() throws Exception {
            mockMvc.perform(put("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("TC-405-003: DELETE /register → 405")
        void shouldReturn405ForDeleteRequest() throws Exception {
            mockMvc.perform(delete("/api/v1/auth/register"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
