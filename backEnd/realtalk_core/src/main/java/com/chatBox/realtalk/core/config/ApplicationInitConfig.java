package com.chatBox.realtalk.core.config;

import com.chatBox.realtalk.base.exception.AppException;
import com.chatBox.realtalk.base.exception.ErrorCode;
import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.enums.IdentityErrorCode;
import com.chatBox.realtalk.core.module.identity.enums.UserRole;
import com.chatBox.realtalk.core.module.identity.repository.AuthenticationRepository;
import com.chatBox.realtalk.core.module.identity.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;


    @Bean
    ApplicationRunner applicationRunner(UserAccountRepository userAccountRepository) {
        return args -> {
            if (userAccountRepository.findBySystemRole(UserRole.ADMIN).isEmpty()) {

                UserAccount userAccount = UserAccount.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .email("datcongh43@gmail.com")
                        .systemRole(UserRole.ADMIN)
                        .build();

                userAccountRepository.save(userAccount);

                log.warn(
                        "Admin user has been created with default password: admin, please change it"
                );
            }
        };
    }

}
