package com.chatBox.realtalk.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.chatBox.realtalk")
public class RealtalkCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealtalkCoreApplication.class, args);
    }
}
