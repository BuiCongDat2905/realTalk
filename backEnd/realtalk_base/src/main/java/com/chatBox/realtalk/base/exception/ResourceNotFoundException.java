package com.chatBox.realtalk.base.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    public String getErrorCode() {
        return "RESOURCE_NOT_FOUND";
    }
}
