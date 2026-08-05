package com.chatBox.realTalk.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        boolean success,
        int status,
        String errorCode,
        String message,
        String path,
        Map<String, String> errors,
        Instant timestamp
) {
}
