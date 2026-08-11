package com.chatBox.realtalk.core.module.identity.service.Impl;

import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.enums.IdentityErrorCode;
import com.chatBox.realtalk.core.module.identity.exception.AppException;
import com.chatBox.realtalk.core.module.identity.repository.UserAccountRepository;
import com.chatBox.realtalk.core.module.identity.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {
    final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public RegisterRes register(RegisterReq registerReq) {
        if(userAccountRepository.existsByUsername(registerReq.getUsername())) {
            throw new AppException(IdentityErrorCode.REGISTER_USERNAME_EXISTS);
        }
        if(userAccountRepository.existsByEmail(registerReq.getEmail())) {
            throw new AppException(IdentityErrorCode.REGISTER_EMAIL_EXISTS);
        }
        UserAccount userAccount = UserAccount.builder()
                .username(registerReq.getUsername())
                .email(registerReq.getEmail())
                .passwordHash(registerReq.getPassword())
                .status(registerReq.getStatus())
                .build();
        userAccountRepository.save(userAccount);

        return RegisterRes.builder()
                .username(userAccount.getUsername())
                .email(userAccount.getEmail())
                .status(userAccount.getStatus())
                .build();
    }
}
