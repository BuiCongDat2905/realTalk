package com.chatBox.realtalk.core.module.identity.service;

import com.chatBox.realtalk.core.module.identity.dto.request.RegisterReq;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import org.springframework.stereotype.Service;

@Service
public interface UserAccountService {
    RegisterRes register(RegisterReq registerReq);
}
