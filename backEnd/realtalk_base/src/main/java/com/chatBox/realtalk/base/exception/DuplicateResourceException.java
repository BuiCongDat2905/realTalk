package com.chatBox.realtalk.base.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    public String getErrorCode() {
        return "DUPLICATE_RESOURCE";
    }
}
