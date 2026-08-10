package com.chatBox.realtalk.core.module.identity.controller;

import com.chatBox.realtalk.base.dto.response.ApiResponse;
import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.chatBox.realtalk.core.module.identity.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {
    final UserAccountService userAccountService;

    @PostMapping("/register")
    public ApiResponse<RegisterRes> register(@Valid @RequestBody RegisterReq registerReq) {
        RegisterRes registerRes = userAccountService.register(registerReq);
        return ApiResponse.<RegisterRes>builder()
                .success(true)
                .message("Đăng ký thành công.")
                .data(registerRes)
                .timestamp(Instant.now())
                .build();
    }
}
