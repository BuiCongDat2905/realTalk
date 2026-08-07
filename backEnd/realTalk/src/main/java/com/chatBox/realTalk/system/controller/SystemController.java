package com.chatBox.realTalk.system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final Environment environment;

    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        return Map.of(
                "activeProfiles",
                Arrays.asList(environment.getActiveProfiles()),
                "defaultProfiles",
                Arrays.asList(environment.getDefaultProfiles())
        );
    }
}