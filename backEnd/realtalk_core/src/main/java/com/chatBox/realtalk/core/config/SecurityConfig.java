//package com.chatBox.realtalk.core.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }
//
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        return http
////                .authorizeHttpRequests(auth -> auth
////                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
////                                "/api/v1/system/**", "/actuator/health").permitAll()
////                        .anyRequest().authenticated()
////                )
////                .build();
////    }
//}
