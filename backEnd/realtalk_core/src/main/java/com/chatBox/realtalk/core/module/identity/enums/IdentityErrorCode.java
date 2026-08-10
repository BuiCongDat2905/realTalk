package com.chatBox.realtalk.core.module.identity.enums;

import com.chatBox.realtalk.base.exception.ErrorCode;
import com.chatBox.realtalk.base.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IdentityErrorCode implements ErrorCode {
    REGISTER_BODY_REQUIRED(
            "REG-VAL-001",
            "Request body đăng ký bị thiếu.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),

    REGISTER_USERNAME_REQUIRED(
            "REG-VAL-002",
            "Username không được để trống.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),

    REGISTER_EMAIL_REQUIRED(
            "REG-VAL-003",
            "Email không được để trống.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),

    REGISTER_EMAIL_INVALID(
            "REG-VAL-004",
            "Email sai định dạng.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),

    REGISTER_PASSWORD_REQUIRED(
            "REG-VAL-005",
            "Password không được để trống.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),

    REGISTER_PASSWORD_TOO_SHORT(
            "REG-VAL-006",
            "Password phải có ít nhất 6 ký tự.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),

    REGISTER_EMAIL_EXISTS(
            "REG-BUS-001",
            "Email đã tồn tại.",
            HttpStatus.CONFLICT,
            ErrorType.BUSINESS_ERROR
    ),

    REGISTER_USERNAME_EXISTS(
            "REG-BUS-002",
            "Username đã tồn tại.",
            HttpStatus.CONFLICT,
            ErrorType.BUSINESS_ERROR
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
    private final ErrorType type;
}
