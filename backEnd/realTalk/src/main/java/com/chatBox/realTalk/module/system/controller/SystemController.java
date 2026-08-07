package com.chatBox.realTalk.module.system.controller;

import com.chatBox.realTalk.base.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final Environment environment;
    private final String applicationName;

    public SystemController(
            Environment environment,
            @Value("${spring.application.name}") String applicationName
    ) {
        this.environment = environment;
        this.applicationName = applicationName;
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile() {
        Map<String, Object> data = Map.of(
                "application", applicationName,
                "activeProfiles", Arrays.asList(environment.getActiveProfiles()),
                "defaultProfiles", Arrays.asList(environment.getDefaultProfiles()),
                "timestamp", Instant.now()
        );
        return ApiResponse.success(data);
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = Map.of(
                "application", applicationName,
                "status", "UP",
                "time", Instant.now()
        );

        return ApiResponse.success(
                "RealTalk Backend đang hoạt động",
                data
        );
    }
}