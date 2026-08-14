package com.example.identity_service.auth.service;

import com.example.identity_service.auth.dto.request.ForgotPasswordRequest;
import com.example.identity_service.auth.dto.request.LoginRequest;
import com.example.identity_service.auth.dto.request.RefreshTokenRequest;
import com.example.identity_service.auth.dto.request.RegisterRequest;
import com.example.identity_service.auth.dto.request.ResetPasswordRequest;
import com.example.identity_service.auth.dto.response.TokenResponse;
import com.example.identity_service.auth.jwt.JwtService;
import com.example.identity_service.email.EmailService;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.user.dto.request.UserCreateRequest;
import com.example.identity_service.user.entity.Role;
import com.example.identity_service.user.entity.User;
import com.example.identity_service.user.mapper.UserMapper;
import com.example.identity_service.user.repository.RoleRepository;
import com.example.identity_service.user.repository.UserRepository;
import com.example.identity_service.user.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        Role userRole =
                roleRepository
                        .findByName("USER")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        UserCreateRequest userCreateRequest = userMapper.toUserCreationRequest(request);
        User user = userService.createUser(userCreateRequest, Set.of(userRole));
        sendVerificationEmail(user);
    }

    public void verifyEmail(String token) {
        String userId = emailVerificationService.getUserId(token);
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        User user =
                userRepository
                        .findById(UUID.fromString(userId))
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationService.deleteToken(token);
    }

    public TokenResponse login(LoginRequest request)
            throws KeyLengthException, JOSEException, ParseException {
        Authentication authentication;
        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            throw new AppException(ErrorCode.USER_ACCOUNT_DISABLED);
        }
        User user = (User) authentication.getPrincipal();
        if (!user.isEmailVerified()) {
            sendVerificationEmail(user);
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        SignedJWT signedJWT = SignedJWT.parse(refreshToken);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Instant expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
        Duration ttl = Duration.between(Instant.now(), expirationTime);
        refreshTokenService.store(jti, user.getId().toString(), ttl);
        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    private void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        emailVerificationService.saveToken(token, user.getId());
        String verificationLink = "http://localhost:8080/api/auth/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }

    public TokenResponse refreshToken(RefreshTokenRequest request)
            throws KeyLengthException, JOSEException, ParseException {
        Jwt jwt = jwtDecoder.decode(request.getRefreshToken());
        if (!"refresh".equals(jwt.getClaimAsString("type"))) {
            throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
        }
        String jti = jwt.getId();
        if (!refreshTokenService.exists(jti)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        User user =
                userRepository
                        .findById(UUID.fromString(jwt.getSubject()))
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        refreshTokenService.revoke(jti);
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        SignedJWT signedJWT = SignedJWT.parse(newRefreshToken);
        String newJti = signedJWT.getJWTClaimsSet().getJWTID();
        Instant expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
        Duration ttl = Duration.between(Instant.now(), expirationTime);
        refreshTokenService.store(newJti, user.getId().toString(), ttl);
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        blacklistAccessToken(accessToken);
        revokeRefreshToken(refreshToken);
    }

    private void blacklistAccessToken(String accessToken) {
        Jwt jwt = jwtDecoder.decode(accessToken);
        String type = jwt.getClaimAsString("type");
        if (!"access".equals(type)) {
            throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
        }
        String jti = jwt.getId();
        if (jti == null) {
            throw new AppException(ErrorCode.MISSING_JTI_CLAIM);
        }
        Duration ttl = Duration.between(Instant.now(), jwt.getExpiresAt());
        tokenBlacklistService.blacklist(jti, ttl);
    }

    private void revokeRefreshToken(String refreshToken) {
        Jwt jwt = jwtDecoder.decode(refreshToken);
        String type = jwt.getClaimAsString("type");
        if (!"refresh".equals(type)) {
            throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
        }
        String jti = jwt.getId();
        if (jti == null) {
            throw new AppException(ErrorCode.MISSING_JTI_CLAIM);
        }
        refreshTokenService.revoke(jti);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String token = UUID.randomUUID().toString();
        emailVerificationService.saveToken(token, user.getId());
        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String userId = emailVerificationService.getUserId(request.getToken());
        if (userId == null) {
            throw new AppException(ErrorCode.USER_ID_NOT_FOUND);
        }
        User user =
                userRepository
                        .findById(UUID.fromString(userId))
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        emailVerificationService.deleteToken(request.getToken());
    }
}
