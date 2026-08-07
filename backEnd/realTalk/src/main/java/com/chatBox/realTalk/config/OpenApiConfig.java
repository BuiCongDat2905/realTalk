package com.chatBox.realTalk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI realTalkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RealTalk Backend API")
                        .description(
                                "Tài liệu API cho hệ thống chat real-time RealTalk"
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RealTalk Development Team"))
                        .license(new License()
                                .name("Private Project")))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}