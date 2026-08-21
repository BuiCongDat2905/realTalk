package com.chatBox.realtalk.core.module.identity.service;

import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import org.springframework.stereotype.Service;

@Service
public interface UserAccountService {
    UserAccount save(UserAccount userAccount);
}
