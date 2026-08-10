package com.chatBox.realtalk.core.module.identity.exception;

import com.chatBox.realtalk.base.exception.ErrorCode;

public class AppException extends com.chatBox.realtalk.base.exception.AppException {

    public AppException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AppException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public AppException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}
