package com.chatBox.realtalk.core.module.identity.controller;

import com.chatBox.realtalk.base.dto.response.ApiResponse;
import com.chatBox.realtalk.core.module.identity.dto.request.IntrospectReq;
import com.chatBox.realtalk.core.module.identity.dto.request.LoginReq;
import com.chatBox.realtalk.core.module.identity.dto.request.LogoutReq;
import com.chatBox.realtalk.core.module.identity.dto.request.RefreshReq;
import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import com.chatBox.realtalk.core.module.identity.dto.response.AuthenticationRes;
import com.chatBox.realtalk.core.module.identity.dto.response.IntrospectRes;
import com.chatBox.realtalk.core.module.identity.dto.response.LoginRes;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.chatBox.realtalk.core.module.identity.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {
    final AuthenticationService authenticationService;

    @PostMapping("/introspect")
    ApiResponse<IntrospectRes> authenticate(@RequestBody IntrospectReq request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectRes>builder()
                .success(true)
                .data(result)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationRes> authenticate(@RequestBody RefreshReq request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationRes>builder()
                .success(true)
                .data(result)
                .timestamp(Instant.now())
                .build();
    }
    @PostMapping("/register")
    public ApiResponse<RegisterRes> register(@Valid @RequestBody RegisterReq registerReq) {
        RegisterRes registerRes = authenticationService.register(registerReq);
        return ApiResponse.<RegisterRes>builder()
                .success(true)
                .message("Register successfully!")
                .data(registerRes)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginRes> login(@Valid @RequestBody LoginReq loginReq) {
        LoginRes loginRes = authenticationService.login(loginReq);
        return ApiResponse.<LoginRes>builder()
                .success(true)
                .message("Login successfully!")
                .data(loginRes)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutReq request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successfully!")
                .timestamp(Instant.now())
                .build();
    }
}
