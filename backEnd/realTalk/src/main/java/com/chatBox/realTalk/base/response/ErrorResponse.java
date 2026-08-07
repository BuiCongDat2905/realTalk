package com.chatBox.realTalk.base.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> errors,
        Instant timestamp
) {
}
