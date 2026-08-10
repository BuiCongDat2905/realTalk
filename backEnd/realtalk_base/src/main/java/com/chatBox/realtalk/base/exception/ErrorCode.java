package com.chatBox.realtalk.base.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getCode();
    String getMessage();
    HttpStatus getStatus();
    ErrorType getType();
}
