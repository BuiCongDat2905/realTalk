package com.chatBox.realTalk.base.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    private final String errorCode;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
