package com.chatBox.realtalk.core.module.identity.service.Impl;

import com.chatBox.realtalk.base.exception.CommonErrorCode;
import com.chatBox.realtalk.core.module.identity.dto.request.*;
import com.chatBox.realtalk.core.module.identity.dto.response.AuthenticationRes;
import com.chatBox.realtalk.core.module.identity.dto.response.IntrospectRes;
import com.chatBox.realtalk.core.module.identity.dto.response.LoginRes;
import com.chatBox.realtalk.core.module.identity.dto.response.RegisterRes;
import com.chatBox.realtalk.core.module.identity.entity.UserAccount;
import com.chatBox.realtalk.core.module.identity.entity.UserSessions;
import com.chatBox.realtalk.core.module.identity.enums.IdentityErrorCode;
import com.chatBox.realtalk.core.module.identity.enums.UserStatus;
import com.chatBox.realtalk.core.module.identity.exception.AppException;
import com.chatBox.realtalk.core.module.identity.repository.UserAccountRepository;
import com.chatBox.realtalk.core.module.identity.repository.UserSessionsRepository;
import com.chatBox.realtalk.core.module.identity.service.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    @NonFinal
    @Value("${app.jwt.signerKey}")
    protected String SIGNER_KEY;
    private static final String TOKEN_ISSUER = "realtalk";
    private static final long ACCESS_TOKEN_TTL_MINUTES = 60;

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final UserSessionsRepository  userSessionsRepository;

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
                .passwordHash(passwordEncoder.encode(registerReq.getPassword()))
                .status(UserStatus.PENDING_VERIFY)
                .build();
        userAccountRepository.save(userAccount);

        return RegisterRes.builder()
                .username(userAccount.getUsername())
                .email(userAccount.getEmail())
                .status(userAccount.getStatus())
                .build();
    }

    @Override
    public LoginRes login(LoginReq loginReq) {
        String username = loginReq.getUsername().trim();

        if(!userAccountRepository.existsByUsername(username) && !userAccountRepository.existsByEmail(username)) {
            throw new AppException(IdentityErrorCode.LOGIN_USERNAME_NOT_EXISTS);
        }
        UserAccount userAccount;
        if(username.contains("@")) {
            userAccount = userAccountRepository.findByEmail(username);

        }else{
            userAccount = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(IdentityErrorCode.LOGIN_USERNAME_NOT_EXISTS));
        }
        if(userAccount == null) {
            throw new AppException(IdentityErrorCode.LOGIN_PASSWORD_NOT_EXITS);
        }
        boolean authenticated = passwordEncoder.matches(loginReq.getPassword(),userAccount.getPasswordHash());
        if(!authenticated) {
            throw new AppException(IdentityErrorCode.LOGIN_PASSWORD_NOT_EXITS);
        }
        var token = generateToken(userAccount);
        return LoginRes.builder()
                .token(token)
                .publicId(userAccount.getPublicId())
                .username(userAccount.getUsername())
                .build();

    }

    //security

    public IntrospectRes introspect(IntrospectReq request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        String role = null;
        try {
            SignedJWT jwt = verifyToken(token);
            role = jwt.getJWTClaimsSet().getStringClaim("scope");
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectRes.builder()
                .valid(isValid)
                .role(role)
                .build();
    }

    public AuthenticationRes authenticate(LoginReq request) {
        var user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(IdentityErrorCode.LOGIN_USERNAME_NOT_EXISTS));
        boolean authenticated = passwordEncoder.matches(request.getPassword(),
                user.getPasswordHash());
        if (!authenticated)
            throw new AppException(CommonErrorCode.UNAUTHENTICATED);
        var token = generateToken(user);
        return  AuthenticationRes.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutReq request) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getToken());

        UserAccount user = userAccountRepository
                .findByUsername(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        byte[] tokenHash = sha256(request.getToken());
        if (userSessionsRepository.existsByTokenHash(tokenHash)) {
            return; 
        }

        userSessionsRepository.save(UserSessions.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant())
                .revokedAt(Instant.now())
                .revokeReason("LOGOUT")
                .build());
    }

    public AuthenticationRes refreshToken(RefreshReq request)
            throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getToken());
        UserAccount user = userAccountRepository
                .findByPublicId(UUID.fromString(signedJWT.getJWTClaimsSet().getSubject()))
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        byte[] oldTokenHash = sha256(request.getToken());
        if (!userSessionsRepository.existsByTokenHash(oldTokenHash)) {
            userSessionsRepository.save(UserSessions.builder()
                    .user(user)
                    .tokenHash(oldTokenHash)
                    .expiresAt(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant())
                    .revokedAt(Instant.now())
                    .revokeReason("REFRESH_ROTATION")
                    .build());
        }

        return AuthenticationRes.builder()
                .token(generateToken(user))
                .authenticated(true)
                .build();
    }

    private String generateToken(UserAccount account) {
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getPublicId().toString())
                .issuer(TOKEN_ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS512).type(JOSEObjectType.JWT).build(),
                jwtClaimsSet);
        try {
            signedJWT.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot sign token", e);
        }
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes(StandardCharsets.UTF_8));

        if (!signedJWT.verify(verifier)) {
            throw new AppException(CommonErrorCode.UNAUTHENTICATED);
        }
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime == null || !expiryTime.after(new Date())) {
            throw new AppException(CommonErrorCode.UNAUTHENTICATED);
        }
        // Kiểm tra token đã bị thu hồi chưa
        if (userSessionsRepository.existsByTokenHash(sha256(token))) {
            throw new AppException(CommonErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    private String buildScope(UserAccount account) {
        return account.getSystemRole().getValue();
    }
    public String extractSubject(String token) throws ParseException, JOSEException {
        return verifyToken(token).getJWTClaimsSet().getSubject();
    }
    private byte[] sha256(String token) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
