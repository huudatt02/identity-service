package com.example.identity_service.auth.jwt;

import com.example.identity_service.auth.service.TokenBlacklistService;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final NimbusJwtDecoder nimbusJwtDecoder;
    private final TokenBlacklistService tokenBlacklistService;

    public CustomJwtDecoder(
            @Value("${jwt.secret-key}") String secretKey,
            TokenBlacklistService tokenBlacklistService) {

        this.tokenBlacklistService = tokenBlacklistService;

        SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        this.nimbusJwtDecoder =
                NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt = nimbusJwtDecoder.decode(token);

        if ("access".equals(jwt.getClaimAsString("type"))) {
            String jti = jwt.getId();

            if (jti == null) {
                throw new AppException(ErrorCode.MISSING_JTI_CLAIM);
            }

            if (tokenBlacklistService.isBlacklisted(jti)) {
                throw new AppException(ErrorCode.TOKEN_BLACKLISTED);
            }
        }

        return jwt;
    }
}
