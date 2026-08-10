package com.chatBox.realtalk.base.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ApiErrorResponse(
        boolean success,
        String code,
        String type,
        String message,
        String path,
        Instant timestamp,
        String field,
        Map<String, String> details,
        String traceId
) {
}
