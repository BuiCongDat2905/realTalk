package com.chatBox.realtalk.base.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    public String getErrorCode() {
        return "BAD_REQUEST";
    }
}
