package com.chatBox.realTalk.base.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

    protected BaseException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
    public abstract String getErrorCode();
}
