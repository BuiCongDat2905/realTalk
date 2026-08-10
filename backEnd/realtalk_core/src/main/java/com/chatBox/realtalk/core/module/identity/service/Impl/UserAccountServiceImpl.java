package com.chatBox.realtalk.core.module.identity.service.Impl;

import com.chatBox.realtalk.base.exception.AppException;
import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.enums.IdentityErrorCode;
import com.chatBox.realtalk.core.module.identity.repository.UserAccountRepository;
import com.chatBox.realtalk.core.module.identity.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {
    final UserAccountRepository userAccountRepository;

    @Override
    public RegisterRes register(RegisterReq registerReq) {
        if (registerReq == null) {
            throw new AppException(IdentityErrorCode.REGISTER_BODY_REQUIRED);
        }

        if (registerReq.getUsername() == null || registerReq.getUsername().isBlank()) {
            throw new AppException(IdentityErrorCode.REGISTER_USERNAME_REQUIRED);
        }
        if (registerReq.getEmail() == null || registerReq.getEmail().isBlank()) {
            throw new AppException(IdentityErrorCode.REGISTER_EMAIL_REQUIRED);
        }
        if (registerReq.getPassword() == null || registerReq.getPassword().isBlank()) {
            throw new AppException(IdentityErrorCode.REGISTER_PASSWORD_REQUIRED);
        }
        if (registerReq.getPassword().length() < 6) {
            throw new AppException(IdentityErrorCode.REGISTER_PASSWORD_TOO_SHORT);
        }

        if (userAccountRepository.existsByEmail(registerReq.getEmail())) {
            throw new AppException(IdentityErrorCode.REGISTER_EMAIL_EXISTS);
        }

        if (userAccountRepository.existsByUsername(registerReq.getUsername())) {
            throw new AppException(IdentityErrorCode.REGISTER_USERNAME_EXISTS);
        }

        UserAccount userAccount = UserAccount.builder()
                .username(registerReq.getUsername())
                .email(registerReq.getEmail())
                .passwordHash(registerReq.getPassword())
                .build();
        userAccountRepository.save(userAccount);

        return RegisterRes.builder()
                .username(userAccount.getUsername())
                .email(userAccount.getEmail())
                .status(userAccount.getStatus())
                .build();
    }
}
