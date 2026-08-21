package com.chatBox.realtalk.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_REQUEST(
            "BASE-VAL-001",
            "Request body không hợp lệ.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),
    PATH_NOT_FOUND(
            "BASE-404-001",
            "Không tìm thấy endpoint.",
            HttpStatus.NOT_FOUND,
            ErrorType.NOT_FOUND_ERROR
    ),
    UNSUPPORTED_METHOD(
            "BASE-405-001",
            "Phương thức HTTP không được hỗ trợ.",
            HttpStatus.METHOD_NOT_ALLOWED,
            ErrorType.SYSTEM_ERROR
    ),
    INVALID_PARAMETER(
            "BASE-VAL-002",
            "Tham số yêu cầu không hợp lệ.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),
    DATA_INTEGRITY_VIOLATION(
            "BASE-DB-001",
            "Dữ liệu không hợp lệ hoặc đã tồn tại.",
            HttpStatus.CONFLICT,
            ErrorType.CONFLICT_ERROR
    ),
    UNEXPECTED_ERROR(
            "BASE-SYS-001",
            "Hệ thống đang gặp sự cố.",
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorType.SYSTEM_ERROR
    ),INVALID_ENUM_VALUE(
            "BASE-VAL-003",
            "Giá trị enum không hợp lệ.",
            HttpStatus.BAD_REQUEST,
            ErrorType.VALIDATION_ERROR
    ),
    UNAUTHENTICATED(
            "BASE-AUTH-01",
            "Tài khoản chưa được xác thực.",
            HttpStatus.UNAUTHORIZED,
            ErrorType.AUTHENTICATION_ERROR
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
    private final ErrorType type;
}
