package com.chatBox.realtalk.core.module.identity.service;

import com.chatBox.realtalk.core.module.identity.dto.request.*;
import com.chatBox.realtalk.core.module.identity.dto.response.AuthenticationRes;
import com.chatBox.realtalk.core.module.identity.dto.response.IntrospectRes;
import com.chatBox.realtalk.core.module.identity.dto.response.LoginRes;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.nimbusds.jose.JOSEException;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public interface AuthenticationService {
    RegisterRes register(RegisterReq registerReq);
    LoginRes login(LoginReq loginReq);
    IntrospectRes introspect(IntrospectReq request)throws JOSEException, ParseException;
    AuthenticationRes authenticate(LoginReq request);
    void logout(LogoutReq request) throws ParseException, JOSEException;
    AuthenticationRes refreshToken(RefreshReq request) throws ParseException, JOSEException;
    String extractSubject(String token) throws ParseException, JOSEException;
}
