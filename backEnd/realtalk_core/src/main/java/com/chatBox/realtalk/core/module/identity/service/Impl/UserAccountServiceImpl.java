package com.chatBox.realtalk.core.module.identity.service.Impl;


import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.repository.UserAccountRepository;
import com.chatBox.realtalk.core.module.identity.service.UserAccountService;
import org.springframework.stereotype.Service;

@Service
public class UserAccountServiceImpl implements UserAccountService {
    UserAccountRepository repository;
    @Override
    public UserAccount save(UserAccount userAccount) {
        repository.save(userAccount);
        return userAccount;
    }
}
