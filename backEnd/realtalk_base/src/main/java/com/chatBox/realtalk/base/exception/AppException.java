package com.chatBox.realtalk.base.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public AppException(ErrorCode errorCode, String detail) {
        super(detail != null ? detail : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public AppException(ErrorCode errorCode, String detail, Throwable cause) {
        super(detail != null ? detail : errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
