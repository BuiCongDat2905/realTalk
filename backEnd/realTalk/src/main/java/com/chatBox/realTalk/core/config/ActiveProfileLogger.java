package com.chatBox.realTalk.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveProfileLogger implements CommandLineRunner {

    private final Environment environment;

    @Override
    public void run(String... args) {
        log.info(
                "Active profiles: {}",
                Arrays.toString(environment.getActiveProfiles())
        );

        log.info(
                "Default profiles: {}",
                Arrays.toString(environment.getDefaultProfiles())
        );
    }
}