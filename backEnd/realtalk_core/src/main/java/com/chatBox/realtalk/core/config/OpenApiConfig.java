package com.chatBox.realtalk.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RealTalk API")
                        .version("1.0.0")
                        .description("API for RealTalk chat application")
                        .contact(new Contact().name("RealTalk Team")));
    }
}
