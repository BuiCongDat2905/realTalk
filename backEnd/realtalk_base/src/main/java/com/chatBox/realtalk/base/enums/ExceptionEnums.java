package com.chatBox.realtalk.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionEnums {
    VALIDATION_ERROR("Lỗi xác thực."),
    ;
    private final String message;
}
